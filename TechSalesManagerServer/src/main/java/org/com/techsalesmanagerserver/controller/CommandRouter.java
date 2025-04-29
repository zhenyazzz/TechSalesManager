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
@RequiredArgsConstructor
public class CommandRouter extends SimpleChannelInboundHandler<JsonMessage> {
    private final Map<String, BiConsumer<ChannelHandlerContext, Map<String, Object>>> commandHandlers = new HashMap<>();

    private final List<Controller> controllers;



    private void initializeHandlers(List<Controller> controllers) {
        for (Controller controller : controllers) {
            if (controller == null) {
                log.warn("Null controller detected, skipping");
                continue;
            }
            Class<?> controllerClass = controller.getClass();
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    String command = method.getAnnotation(Command.class).value();
                    if (command == null || command.trim().isEmpty()) {
                        log.warn("Empty or null command in method {} of controller {}, skipping", method.getName(), controllerClass.getSimpleName());
                        continue;
                    }
                    if (commandHandlers.containsKey(command)) {
                        throw new IllegalStateException(String.format("Duplicate command '%s' in method %s of controller %s",
                                command, method.getName(), controllerClass.getSimpleName()));
                    }
                    // Проверка сигнатуры метода
                    if (!isValidCommandMethod(method)) {
                        log.warn("Invalid method signature for command {} in method {} of controller {}, skipping",
                                command, method.getName(), controllerClass.getSimpleName());
                        continue;
                    }
                    commandHandlers.put(command, (ctx, data) -> {
                        try {
                            method.invoke(controller, ctx, data);
                        } catch (Exception e) {
                            log.error("Error executing command {} in controller {}: {}",
                                    command, controllerClass.getSimpleName(), e.getMessage(), e);
                            sendError(ctx, "Internal server error: " + e.getMessage());
                        }
                    });
                    log.info("Registered command '{}' for method {} in controller {}",
                            command, method.getName(), controllerClass.getSimpleName());
                }
            }
        }
    }

    private boolean isValidCommandMethod(Method method) {
        // Проверка, что метод имеет правильную сигнатуру: (ChannelHandlerContext, Map<String, Object>)
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 2 &&
                ChannelHandlerContext.class.isAssignableFrom(parameterTypes[0]) &&
                Map.class.isAssignableFrom(parameterTypes[1]);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JsonMessage msg) {
        System.out.println("sybau");
        //System.out.println(msg.toString());
        //log.info("sybau");
        if (msg == null) {
            log.warn("Received null message");
            sendError(ctx, "Invalid message: null");
            return;
        }

        String command = msg.getCommand();
        if (command == null || command.trim().isEmpty()) {
            log.warn("Received message with null or empty command");
            sendError(ctx, "Invalid message: missing or empty command");
            return;
        }

        BiConsumer<ChannelHandlerContext, Map<String, Object>> handler = commandHandlers.get(command);
        if (handler == null) {
            log.warn("No handler found for command: {}", command);
            sendError(ctx, "Unknown command: " + command);
            return;
        }

        try {
            System.out.println("sybau");
            handler.accept(ctx, msg.getData() != null ? msg.getData() : new HashMap<>());
            System.out.println("sybau");
            log.debug("Successfully executed command: {}", command);
        } catch (Exception e) {
            log.error("Unexpected error while handling command {}: {}", command, e.getMessage(), e);
            sendError(ctx, "Internal server error: " + e.getMessage());
        }
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