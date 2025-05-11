package org.com.techsalesmanagerserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.Request;
import org.com.techsalesmanagerserver.dto.Response;
import org.com.techsalesmanagerserver.enumeration.RequestType;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.com.techsalesmanagerserver.service.SupplierService;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierController implements Controller {
    private final SupplierService supplierService;

    @Command(RequestType.GET_ALL_SUPPLIERS)
    public void handleGet(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling GET_ALL_SUPPLIERS request");
        writer.println(JsonUtils.toJson(supplierService.findAll()));
    }

    @Command(RequestType.CREATE_SUPPLIER)
    public void handleCreate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling CREATE_SUPPLIER request");
        writer.println(JsonUtils.toJson(supplierService.save(request)));
    }

    @Command(RequestType.DELETE_SUPPLIER)
    public void handleDelete(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling DELETE_SUPPLIER request");
        writer.println(JsonUtils.toJson(supplierService.deleteById(request)));
    }

    @Command(RequestType.UPDATE_SUPPLIER)
    public void handleUpdate(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling UPDATE_SUPPLIER request");
        writer.println(JsonUtils.toJson(supplierService.update(request)));
    }

    @Command(RequestType.SEARCH_SUPPLIER)
    public void handleFind(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling SEARCH_SUPPLIER request");
        writer.println(JsonUtils.toJson(supplierService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_SUPPLIER_BY_ID)
    public void handleFilterById(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_SUPPLIER_BY_ID request");
        writer.println(JsonUtils.toJson(supplierService.findById(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.FILTER_SUPPLIER_BY_NAME)
    public void handleFilterByName(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_SUPPLIER_BY_NAME request");
        writer.println(JsonUtils.toJson(supplierService.findByName(request.getBody())));
    }

    @Command(RequestType.FILTER_SUPPLIER_BY_PRODUCT)
    public void handleFilterByProduct(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling FILTER_SUPPLIER_BY_PRODUCT request");
        writer.println(JsonUtils.toJson(supplierService.findByProduct(JsonUtils.fromJson(request.getBody(), Long.class))));
    }

    @Command(RequestType.ADD_PRODUCT_TO_SUPPLIER)
    public void handleAddProduct(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling ADD_PRODUCT_TO_SUPPLIER request");
        writer.println(JsonUtils.toJson(supplierService.addProductToSupplier(request)));
    }

    @Command(RequestType.REMOVE_PRODUCT_FROM_SUPPLIER)
    public void handleRemoveProduct(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling REMOVE_PRODUCT_FROM_SUPPLIER request");
        writer.println(JsonUtils.toJson(supplierService.removeProductFromSupplier(request)));
    }

    @Command(RequestType.GET_SUPPLIER_PRODUCTS)
    public void handleGetProducts(PrintWriter writer, Request request) throws JsonProcessingException {
        log.info("Handling GET_SUPPLIER_PRODUCTS request");
        writer.println(JsonUtils.toJson(supplierService.getSupplierProducts(JsonUtils.fromJson(request.getBody(), Long.class))));
    }
} 