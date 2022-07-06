package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.*;
import com.geekbrains.cloud.netty.CloudServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverStorage;

    // private Path rootDir=Paths.get("server_files#1").toAbsolutePath();
    private Path rootDir;

    public Path getRootDir() {
        return rootDir;
    }

    public void setRootDir(Path rootDir) {
        this.rootDir = rootDir;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    private String nick;

    private AuthService authService;


    public CloudFileHandler() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

       // ctx.writeAndFlush(new ListFiles(serverStorage));

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (!SQLHandler.connect()) {
            throw new RuntimeException("Не удалось подключить к БД");
        }
        authService = new DBAuthService();

        if (cloudMessage instanceof Reg reg) {

            if (authService.registration(reg.login, reg.password, reg.nick)) {
                System.out.println("GOOD");
            }
        }


        if (cloudMessage instanceof Authentification authentification) {
            if (!SQLHandler.connect()) {
                throw new RuntimeException("Не удалось подключить к БД");
            }
            authService = new DBAuthService();
            // получаем путь к индивидуальной  папке пользователя

            Integer id = authService.getIdByLoginAndPassword(authentification.login, authentification.password);
            setRootDir(Paths.get("server_files#" + id).toAbsolutePath());
            serverStorage = getRootDir();

            // получаем путь к индивидуальной  папке пользователя

            String nickname = authService
                    .getNicknameByLoginAndPassword(authentification.login, authentification.password);
            if (nickname != null) {

                ctx.writeAndFlush(new Authentification(true));
                ctx.writeAndFlush(new FileRequest(nickname));
                ctx.writeAndFlush(new ListFiles(serverStorage));

            } else {
                ctx.writeAndFlush(new Authentification(false));
            }
        }

        // сигнал от сервера на загрузку файла с сервера на клиент
        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(serverStorage.resolve(fileRequest.getName())));
            System.out.println(serverStorage.resolve(fileRequest.getName()) + "FileRequestToClient");
        }
        // сигнал от клиента на загрузку файла с клиента на сервер
        if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(serverStorage.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(serverStorage));
        }
        // навигация на сервере. Заходим в папку
        if (cloudMessage instanceof PathInRequest pathInRequest) {
            Path resolve = serverStorage.resolve(Path.of(pathInRequest.getPathIn()));
            ctx.writeAndFlush(new ListFiles(resolve));
        }
        // навигаци на сервере. Выходим на родителя
        if (cloudMessage instanceof PathUpRequest pathUpRequest) {
            if (!rootDir.getParent().equals(Path.of(pathUpRequest.getPathUp()))) {
                System.out.println(rootDir);
                System.out.println(pathUpRequest.getPathUp());
                ctx.writeAndFlush(new ListFiles(Path.of(pathUpRequest.getPathUp())));
            }
        }
        if (cloudMessage instanceof ChangePathServerRequest changePathServerRequest) {
            serverStorage = Paths.get(changePathServerRequest.getPath());
        }
    }

}
