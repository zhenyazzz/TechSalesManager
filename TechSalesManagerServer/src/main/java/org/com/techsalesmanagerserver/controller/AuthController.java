package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.com.techsalesmanagerserver.dto.Request;
import org.com.techsalesmanagerserver.dto.Response;
import org.com.techsalesmanagerserver.enumeration.RequestType;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.com.techsalesmanagerserver.service.UserService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

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
        writer.println(JsonUtils.toJson(userService.deleteById(request)));
    }

    @Command(RequestType.UPDATE_USER)
    public void handleUpdate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Received command: {}", request.getType());
        writer.println(JsonUtils.toJson(userService.updateUser(request)));
        log.info("user updated");
    }

    @Command(RequestType.SEARCH_USER)
    public void handleFind(PrintWriter writer, Request request) throws JsonProcessingException {
        writer.println(JsonUtils.toJson(userService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_USER_BY_ID)
    public void handleSearchById(PrintWriter writer, Request request) throws IOException, ClassNotFoundException, TimeoutException {
        log.info("Handling FILTER_USER_BY_ID_RANGE request: {}", request);
        writer.println(JsonUtils.toJson(userService.filterById(request)));
    }
    @Command(RequestType.FILTER_USER_BY_EMAIL)
    public void handleSearchByEmail(PrintWriter writer, Request request) throws IOException, ClassNotFoundException, TimeoutException {
        log.info("Handling FILTER_USER_BY_EMAIL request: {}", request);
        writer.println(JsonUtils.toJson(userService.filterByEmail(request)));
    }
}
