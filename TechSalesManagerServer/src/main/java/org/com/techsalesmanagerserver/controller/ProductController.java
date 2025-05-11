package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.Request;
import org.com.techsalesmanagerserver.dto.Response;
import org.com.techsalesmanagerserver.enumeration.RequestType;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.com.techsalesmanagerserver.service.ProductService;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductController implements Controller {
    private final ProductService productService;

    @Command(RequestType.GET_ALL_PRODUCTS)
    public void handleGet(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling GET_ALL_PRODUCTS request");
        writer.println(JsonUtils.toJson(productService.findAll()));
    }

    @Command(RequestType.CREATE_PRODUCT)
    public void handleCreate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling CREATE_PRODUCT request");
        writer.println(JsonUtils.toJson(productService.save(request)));
    }

    @Command(RequestType.DELETE_PRODUCT)
    public void handleDelete(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling DELETE_PRODUCT request");
        writer.println(JsonUtils.toJson(productService.deleteById(request)));
    }

    @Command(RequestType.UPDATE_PRODUCT)
    public void handleUpdate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling UPDATE_PRODUCT request");
        writer.println(JsonUtils.toJson(productService.update(request)));
    }

    @Command(RequestType.SEARCH_PRODUCT)
    public void handleFind(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling SEARCH_PRODUCT request");
        writer.println(JsonUtils.toJson(productService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_PRODUCT_BY_ID)
    public void handleFilterById(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_PRODUCT_BY_ID request");
        //writer.println(JsonUtils.toJson(productService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
        writer.println(JsonUtils.toJson(productService.filterById(request)));
    }

    @Command(RequestType.FILTER_PRODUCT_BY_NAME)
    public void handleFilterByName(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_PRODUCT_BY_NAME request");
       // writer.println(JsonUtils.toJson(productService.findByName(request.getBody())));
        writer.println(JsonUtils.toJson(productService.filterByName(request)));
    }

    @Command(RequestType.FILTER_PRODUCT_BY_CATEGORY)
    public void handleFilterByCategory(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_PRODUCT_BY_CATEGORY request");
        writer.println(JsonUtils.toJson(productService.findByCategory(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_PRODUCT_BY_SUPPLIER)
    public void handleFilterBySupplier(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_PRODUCT_BY_SUPPLIER request");
        writer.println(JsonUtils.toJson(productService.findBySupplier(JsonUtils.fromJson(request.getBody(), Long.class))));
    }
} 