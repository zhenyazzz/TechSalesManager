package org.com.techsalesmanagerserver.controller;

import io.netty.channel.ChannelHandlerContext;
import org.com.techsalesmanagerserver.server.init.JsonMessage;

import java.util.Map;

public abstract class CommandController {
    // Общий метод для отправки успешного ответа
    protected void sendSuccess(ChannelHandlerContext ctx, Map<String, Object> data) {
        JsonMessage response = new JsonMessage();
        response.setCommand("success");
        response.setData(data);
        ctx.writeAndFlush(response);
    }

    // Общий метод для отправки ошибки
    protected void sendError(ChannelHandlerContext ctx, String reason) {
        JsonMessage response = new JsonMessage();
        response.setCommand("error");
        response.getData().put("reason", reason);
        ctx.writeAndFlush(response);
    }
}
