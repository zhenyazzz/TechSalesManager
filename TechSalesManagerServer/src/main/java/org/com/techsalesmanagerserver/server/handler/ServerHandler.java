package org.com.techsalesmanagerserver.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.com.techsalesmanagerserver.server.init.JsonMessage;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class ServerHandler extends SimpleChannelInboundHandler<JsonMessage> {
    private final UserHandler userHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JsonMessage msg) {
        userHandler.handle(ctx, msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
