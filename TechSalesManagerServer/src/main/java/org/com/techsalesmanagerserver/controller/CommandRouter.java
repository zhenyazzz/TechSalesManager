package org.com.techsalesmanagerserver.controller;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.com.techsalesmanagerserver.server.init.JsonMessage;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CommandRouter extends SimpleChannelInboundHandler<JsonMessage> {
    private final Map<String, BiConsumer<ChannelHandlerContext, Map<String, Object>>> commandHandlers = new HashMap<>();

    public CommandRouter(List<CommandController> controllers) {
        // Сканируем все контроллеры и собираем методы с @Command
        for (CommandController controller : controllers) {
            for (Method method : controller.getClass().getMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    String command = method.getAnnotation(Command.class).value();
                    commandHandlers.put(command, (ctx, data) -> {
                        try {
                            method.invoke(controller, ctx, data);
                        } catch (Exception e) {
                            sendError(ctx, "Internal server error");
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JsonMessage msg) {
        String command = msg.getCommand();
        BiConsumer<ChannelHandlerContext, Map<String, Object>> handler = commandHandlers.get(command);

        if (handler != null) {
            handler.accept(ctx, msg.getData());
        } else {
            sendError(ctx, "Unknown command: " + command);
        }
    }

    private void sendError(ChannelHandlerContext ctx, String reason) {
        JsonMessage response = new JsonMessage();
        response.setCommand("error");
        response.getData().put("reason", reason);
        ctx.writeAndFlush(response);
    }
}
