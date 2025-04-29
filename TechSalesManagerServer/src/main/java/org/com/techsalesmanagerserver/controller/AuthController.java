package org.com.techsalesmanagerserver.controller;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.com.techsalesmanagerserver.model.User;
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
        System.out.println("логин происходит");
        //JsonMessage response = userService.authenticate(username, password);
        JsonMessage response = new JsonMessage();
        response.setCommand("success");
        response.setData(new HashMap<String,Object>() {{
            put("role", "user");
        }});

        ctx.writeAndFlush(response);
    }

    @Command("register")
    public void handleRegister(ChannelHandlerContext ctx, Map<String, Object> data) {
        User user = new User();
        user.setName(data.get("name").toString());
        user.setSurname(data.get("surname").toString());
        user.setUsername(data.get("username").toString());
        user.setEmail(data.get("email").toString());
        user.setPassword(data.get("password").toString());

        JsonMessage response = userService.register(user);
        ctx.writeAndFlush(response);
    }
}
