package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.CloudMessage;
import com.geekbrains.cloud.FileMessage;
import com.geekbrains.cloud.FileRequest;
import com.geekbrains.cloud.ListFiles;
import com.geekbrains.cloud.june.cloudapplication.FileInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverStorage;

    public CloudFileHandler() {
         serverStorage = Paths.get("C:\\Users\\anasonov\\IdeaProjects\\Cloud\\geek-cloud-2022-june-master\\server_files");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {


        ctx.writeAndFlush(new ListFiles(serverStorage));

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(serverStorage.resolve(fileRequest.getName())));
            System.out.println(serverStorage.resolve(fileRequest.getName()));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(serverStorage.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(serverStorage));
        }
    }
}
