package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.Request;
import org.com.techsalesmanagerserver.dto.Response;
import org.com.techsalesmanagerserver.enumeration.RequestType;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.com.techsalesmanagerserver.service.OrderService;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderController implements Controller {
    private final OrderService orderService;

    @Command(RequestType.GET_ALL_ORDERS)
    public void handleGet(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling GET_ALL_ORDERS request");
        writer.println(JsonUtils.toJson(orderService.findAll()));
    }

    @Command(RequestType.CREATE_ORDER)
    public void handleCreate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling CREATE_ORDER request");
        writer.println(JsonUtils.toJson(orderService.save(request)));
    }

    @Command(RequestType.DELETE_ORDER)
    public void handleDelete(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling DELETE_ORDER request");
        writer.println(JsonUtils.toJson(orderService.deleteById(request)));
    }

    @Command(RequestType.UPDATE_ORDER)
    public void handleUpdate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling UPDATE_ORDER request");
        writer.println(JsonUtils.toJson(orderService.update(request)));
    }

    @Command(RequestType.SEARCH_ORDER)
    public void handleFind(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling SEARCH_ORDER request");
        writer.println(JsonUtils.toJson(orderService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_ORDER_BY_ID)
    public void handleFilterById(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_ORDER_BY_ID request");
        writer.println(JsonUtils.toJson(orderService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_ORDER_BY_USER)
    public void handleFilterByUser(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_ORDER_BY_USER request");
        writer.println(JsonUtils.toJson(orderService.findByUser(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_ORDER_BY_STATUS)
    public void handleFilterByStatus(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_ORDER_BY_STATUS request");
        writer.println(JsonUtils.toJson(orderService.findByStatus(request.getBody())));
    }

   /* @Command(RequestType.FILTER_ORDER_BY_DATE_RANGE)
    public void handleFilterByDateRange(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_ORDER_BY_DATE_RANGE request");
        writer.println(JsonUtils.toJson(orderService.findByDateRange(request.getBody())));
    }*/
} 