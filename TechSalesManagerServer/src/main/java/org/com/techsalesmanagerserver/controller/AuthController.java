package org.com.techsalesmanagerserver.controller;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.com.techsalesmanagerserver.server.init.JsonMessage;
import org.com.techsalesmanagerserver.service.UserService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthController implements Controller{
    private final UserService userService;

    @Command("login")
    public void handleLogin(ChannelHandlerContext ctx, Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");

        JsonMessage response = userService.authenticate(username, password);
        ctx.writeAndFlush(response);
    }

    @Command("register")
    public void handleRegister(ChannelHandlerContext ctx, Map<String, Object> data) {

    }
}
