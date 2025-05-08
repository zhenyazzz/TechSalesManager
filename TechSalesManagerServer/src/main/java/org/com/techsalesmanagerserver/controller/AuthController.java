package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.enumeration.RequestType;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.com.techsalesmanagerserver.server.Request;
import org.com.techsalesmanagerserver.server.Response;
import org.com.techsalesmanagerserver.service.UserService;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthController implements Controller{
    private final UserService userService;

    //авторизация/регистрация
    @Command(RequestType.AUTHORIZATION)
    public void handleLogin(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(JsonUtils.toJson(userService.authenticate(request)));
    }

    @Command(RequestType.REGISTRATION)
    public void handleRegister(PrintWriter writer, Request request) throws JsonProcessingException {
        System.out.println(request);
        writer.println(JsonUtils.toJson(userService.register(request)));
    }

    //круд пользователя

    @Command(RequestType.GET_ALL_USERS)
    public void handleGet(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(JsonUtils.toJson(userService.findAll()));
    }

    @Command(RequestType.CREATE_USER)
    public void handleCreate(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(JsonUtils.toJson(userService.createUser(request)));
    }

    @Command(RequestType.DELETE_USER)
    public void handleDelete(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(userService.deleteById(request));
    }

    @Command(RequestType.UPDATE_USER)
    public void handleUpdate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Received command: {}", request.getType());
        writer.println(userService.updateUser(request));
        log.info("user updated");
    }

    @Command(RequestType.SEARCH_USER)
    public void handleFind(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(userService.findById(JsonUtils.fromJson(request.getBody(), Long.class)));
    }
}
