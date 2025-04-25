package org.com.techsalesmanagerserver.server.handler;

import io.netty.channel.ChannelHandlerContext;
import org.com.techsalesmanagerserver.server.init.JsonMessage;

public interface MessageHandler {
    void handle(ChannelHandlerContext ctx, JsonMessage msg);
}
