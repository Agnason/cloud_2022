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

    public CloudFileHandler() {
        serverStorage = Paths.get("server_files").toAbsolutePath();

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        ctx.writeAndFlush(new ListFiles(serverStorage));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof Authentification authentification) {
            SimpleAuthService simpleAuthService=new SimpleAuthService();
            String nickname = simpleAuthService
                    .getNicknameByLoginAndPassword(authentification.login, authentification.password);
            if( nickname !=null){
                ctx.writeAndFlush(new Authentification(true));
                ctx.writeAndFlush(new FileRequest(nickname));
            }else {
                ctx.writeAndFlush(new Authentification(false));
            }

        }

        // сигнал от сервера на загрузку файла с сервера на клиент
        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(serverStorage.resolve(fileRequest.getName())));
            System.out.println(serverStorage.resolve(fileRequest.getName()));
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
            System.out.println(resolve);
        }
        // навигаци на сервере. Выходим на родителя
        if (cloudMessage instanceof PathUpRequest pathUpRequest) {

            ctx.writeAndFlush(new ListFiles(Path.of(pathUpRequest.getPathUp())));
        }
        if (cloudMessage instanceof ChangePathServerRequest changePathServerRequest) {
            serverStorage=Paths.get(changePathServerRequest.getPath());
        }
    }
}
