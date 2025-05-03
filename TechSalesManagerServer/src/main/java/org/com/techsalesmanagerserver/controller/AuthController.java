package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.com.techsalesmanagerserver.server.Request;
import org.com.techsalesmanagerserver.service.UserService;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthController implements Controller{
    private final UserService userService;

    //авторизация/регистрация
    @Command("login")
    public void handleLogin(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(userService.authenticate(request));
    }

    @Command("register")
    public void handleRegister(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(userService.register(request));
    }

    //круд пользователя
    @Command("get_users")
    public void handleGet(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(userService.findAll());
    }

    @Command("create_user")
    public void handleCreate(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(userService.createUser(request));
    }

    @Command("delete_user")
    public void handleDelete(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(userService.deleteById(request));
    }

    @Command("update_user")
    public void handleUpdate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Received command: {}", request.getType());
        writer.println(userService.updateUser(request));
        log.info("user updated");
    }

    @Command("search_user")
    public void handleFind(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(userService.findById(JsonUtils.fromJson(request.getBody(), Long.class)));
    }
}
