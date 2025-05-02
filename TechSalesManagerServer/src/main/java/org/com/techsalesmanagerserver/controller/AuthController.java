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



    //авторизация/регистрация
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
        user.setName(data.toString());
        user.setSurname(data.get("surname").toString());
        user.setUsername(data.get("username").toString());
        user.setEmail(data.get("email").toString());
        user.setPassword(data.get("password").toString());
        JsonMessage response = userService.register(user);
        ctx.writeAndFlush(response);
    }



    //круд пользователя
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

        log.info("Received command: {}", data);

        User user = new User();
        String name = data.get("name").toString();
        String surname = data.get("surname").toString();
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        String email = (String) data.get("email");
        String role = (String) data.get("role");

        user.setName(name);
        user.setSurname(surname);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.valueOf(role));
        user.setOrders(null);
        userService.save(user);
        JsonMessage response = new JsonMessage();
        response.setCommand("success");
        response.getData().put("user",user);
        ctx.writeAndFlush(response);
        log.info("user saved");
    }

    @Command("delete_user")
    public void handleDelete(ChannelHandlerContext ctx, Map<String, Object> data) {
        userService.deleteById(Long.parseLong(data.get("id").toString()));
        log.info("user deleted");
        JsonMessage response = new JsonMessage();
        response.setCommand("success");
        ctx.writeAndFlush(response);
    }

    @Command("update_user")
    public void handleUpdate(ChannelHandlerContext ctx, Map<String, Object> data) {
        log.info("Received command: {}", data);

        User user = new User();
        Long id = Long.parseLong(data.get("id").toString());
        String name = (String) data.get("name");
        String surname = (String) data.get("surname");
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        String email = (String) data.get("email");
        String role = (String) data.get("role");

        user.setId(id);
        user.setName(name);
        user.setSurname(surname);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.valueOf(role));
        user.setOrders(null);


        userService.updateUser(user);

        JsonMessage response = new JsonMessage();
        response.setCommand("success");
        response.getData().put("user",user);
        ctx.writeAndFlush(response);
        log.info("user updated");
        //update


    }



    //работа с пользователем

    @Command("search_user")
    public void handleFind(ChannelHandlerContext ctx, Map<String, Object> data) {
        JsonMessage response = userService.findById(Long.parseLong(data.get("id").toString()));
        ctx.writeAndFlush(response);
    }
}
