package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Logger;
import org.com.techsalesmanagerserver.model.Role;
import org.com.techsalesmanagerserver.model.User;
import org.com.techsalesmanagerserver.server.init.JsonMessage;
import org.com.techsalesmanagerserver.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthController implements Controller{
    private final UserService userService;

    @Command("login")
    public void handleLogin(ChannelHandlerContext ctx, Map<String, Object> data) {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        System.out.println("логин происходит");
        System.out.println(username);
        System.out.println(password);
        JsonMessage response = userService.authenticate(username, password);

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

    @Command("get_users")
    public void handleGet(ChannelHandlerContext ctx, Map<String, Object> data) {

        JsonMessage cmd=new JsonMessage();
        cmd.setCommand(data.get("command").toString());
        log.debug("Received command: {}", cmd);


        /*// Начало потока
        JsonMessage startResponse = new JsonMessage();
        startResponse.setCommand("start_users");
        ctx.writeAndFlush(startResponse);*/

        List<User> users = userService.findAll_List();


        /*// Отправка каждого пользователя
        for (User user : users) {
            user.setOrders(null);
            JsonMessage userResponse = new JsonMessage();
            userResponse.setCommand("user");
            userResponse.getData().put("user", user);
            ctx.writeAndFlush(userResponse);
        }

        // Конец потока
        JsonMessage endResponse = new JsonMessage();
        endResponse.setCommand("end_users");
        ctx.writeAndFlush(endResponse);*/


        // Создание одного JsonMessage со списком пользователей
        JsonMessage response = new JsonMessage();
        response.setCommand("users");
        response.getData().put("users", users);
        log.debug("Sending response: {}", response);
        ctx.writeAndFlush(response);

    }

    @Command("create_user")
    public void handleCreate(ChannelHandlerContext ctx, Map<String, Object> data) {
        log.debug("Received command: {}", data);
        User user = new User();
        user.setName(data.get("name").toString());
        user.setSurname(data.get("surname").toString());
        user.setUsername(data.get("username").toString());
        user.setEmail(data.get("email").toString());
        user.setPassword(data.get("password").toString());
        userService.save(user);
        log.info("user saved");
    }


}
