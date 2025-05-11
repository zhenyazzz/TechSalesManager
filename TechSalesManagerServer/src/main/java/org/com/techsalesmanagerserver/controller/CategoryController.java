package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.Request;
import org.com.techsalesmanagerserver.dto.Response;
import org.com.techsalesmanagerserver.enumeration.RequestType;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.com.techsalesmanagerserver.service.CategoryService;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryController implements Controller {
    private final CategoryService categoryService;

    @Command(RequestType.GET_ALL_CATEGORIES)
    public void handleGet(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling GET_ALL_CATEGORIES request");
        writer.println(JsonUtils.toJson(categoryService.findAll()));
    }

    @Command(RequestType.CREATE_CATEGORY)
    public void handleCreate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling CREATE_CATEGORY request");
        writer.println(JsonUtils.toJson(categoryService.save(request)));
    }

    @Command(RequestType.DELETE_CATEGORY)
    public void handleDelete(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling DELETE_CATEGORY request");
        writer.println(JsonUtils.toJson(categoryService.deleteById(request)));
    }

    @Command(RequestType.UPDATE_CATEGORY)
    public void handleUpdate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling UPDATE_CATEGORY request");
        writer.println(JsonUtils.toJson(categoryService.update(request)));
    }

    @Command(RequestType.SEARCH_CATEGORY)
    public void handleFind(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling SEARCH_CATEGORY request");
        writer.println(JsonUtils.toJson(categoryService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_CATEGORY_BY_ID)
    public void handleFilterById(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_CATEGORY_BY_ID request");
        writer.println(JsonUtils.toJson(categoryService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_CATEGORY_BY_NAME)
    public void handleFilterByName(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_CATEGORY_BY_NAME request");
        writer.println(JsonUtils.toJson(categoryService.findByName(request.getBody())));
    }
} 