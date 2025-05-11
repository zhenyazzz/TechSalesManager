package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.Request;
import org.com.techsalesmanagerserver.dto.Response;
import org.com.techsalesmanagerserver.enumeration.RequestType;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.com.techsalesmanagerserver.service.OrderItemService;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderItemController implements Controller {
    private final OrderItemService orderItemService;

    @Command(RequestType.GET_ALL_ORDER_ITEMS)
    public void handleGet(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling GET_ALL_ORDER_ITEMS request");
        writer.println(JsonUtils.toJson(orderItemService.findAll()));
    }

    @Command(RequestType.CREATE_ORDER_ITEM)
    public void handleCreate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling CREATE_ORDER_ITEM request");
        writer.println(JsonUtils.toJson(orderItemService.save(request)));
    }

    @Command(RequestType.DELETE_ORDER_ITEM)
    public void handleDelete(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling DELETE_ORDER_ITEM request");
        writer.println(JsonUtils.toJson(orderItemService.deleteById(request)));
    }

    @Command(RequestType.UPDATE_ORDER_ITEM)
    public void handleUpdate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling UPDATE_ORDER_ITEM request");
        writer.println(JsonUtils.toJson(orderItemService.update(request)));
    }

    @Command(RequestType.SEARCH_ORDER_ITEM)
    public void handleFind(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling SEARCH_ORDER_ITEM request");
        writer.println(JsonUtils.toJson(orderItemService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_ORDER_ITEM_BY_ID)
    public void handleFilterById(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_ORDER_ITEM_BY_ID request");
        writer.println(JsonUtils.toJson(orderItemService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_ORDER_ITEM_BY_ORDER)
    public void handleFilterByOrder(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_ORDER_ITEM_BY_ORDER request");
        writer.println(JsonUtils.toJson(orderItemService.findByOrder(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

} 