package org.com.techsalesmanagerserver.controller;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.server.init.JsonMessage;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class CommandRouter extends SimpleChannelInboundHandler<JsonMessage> {
    private final Map<String, BiConsumer<ChannelHandlerContext, Map<String, Object>>> commandHandlers = new HashMap<>();

    private final List<Controller> controllers;

    public CommandRouter(List<Controller> controllers) {
        this.controllers = controllers;
        initializeHandlers(controllers);
    }


    private void initializeHandlers(List<Controller> controllers) {
        for (Controller controller : controllers) {
            if (controller == null) continue;
            Class<?> clazz = controller.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    String command = method.getAnnotation(Command.class).value();
                    if (command == null || command.trim().isEmpty()) continue;
                    if (commandHandlers.containsKey(command)) {
                        throw new IllegalStateException("Duplicate command: " + command);
                    }
                    if (!isValidCommandMethod(method)) continue;

                    method.setAccessible(true);
                    commandHandlers.put(command, (ctx, data) -> {
                        try {
                            method.invoke(controller, ctx, data);
                        } catch (Exception e) {
                            // Логируем и отправляем ошибку
                            sendError(ctx, "Ошибка при выполнении команды: " + e.getMessage());
                        }
                    });
                }
            }
        }
    }

    private boolean isValidCommandMethod(Method method) {
        Class<?>[] params = method.getParameterTypes();
        return params.length == 2 &&
                ChannelHandlerContext.class.isAssignableFrom(params[0]) &&
                Map.class.isAssignableFrom(params[1]);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JsonMessage msg) {
        if (msg == null || msg.getCommand() == null) {
            sendError(ctx, "Неверное сообщение");
            return;
        }
        BiConsumer<ChannelHandlerContext, Map<String, Object>> handler = commandHandlers.get(msg.getCommand());
        if (handler == null) {
            sendError(ctx, "Неизвестная команда: " + msg.getCommand());
            return;
        }
        handler.accept(ctx, msg.getData() != null ? msg.getData() : new HashMap<>());
    }

    private void sendError(ChannelHandlerContext ctx, String reason) {
        JsonMessage response = new JsonMessage();
        response.setCommand("error");
        response.getData().put("reason", reason);
        ctx.writeAndFlush(response);
        log.debug("Sent error response: {}", reason);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Channel error: {}", cause.getMessage(), cause);
        sendError(ctx, "Server error: " + cause.getMessage());
        ctx.close();
    }
}