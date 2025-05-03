package org.com.techsalesmanagerserver.server;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.com.techsalesmanagerserver.controller.Command;
import org.com.techsalesmanagerserver.controller.Controller;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerHandler implements Runnable {
    private final List<Controller> controllers;
    private final Map<String, Method> commandMap = new HashMap<>();
    protected Socket clientSocket = null;
    BufferedReader reader = null;
    PrintWriter writer = null;

    public ServerHandler(List<Controller> controllers, Socket clientSocket) throws IOException {
        this.controllers = controllers;
        this.clientSocket = clientSocket;
        writer = new PrintWriter(clientSocket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        setupCommandMap();
    }

    private void setupCommandMap() {
        for (Controller controller : controllers) {
            for (Method method : controller.getClass().getMethods()) {
                Command cmd = method.getAnnotation(Command.class);
                if (cmd != null) {
                    commandMap.put(cmd.value(), method);
                }
            }
        }
    }

    private Controller findController(Method method) {
        for (Controller controller : controllers) {
            if (method.getDeclaringClass().isInstance(controller)) {
                return controller;
            }
        }
        throw new RuntimeException("Controller not found for method " + method.getName());
    }

    private void invokeHandler(Method handler, PrintWriter writer, Request request) throws JsonProcessingException {
        try {
            handler.invoke(findController(handler), writer, request);
        } catch (Exception e) {
            sendError(writer, "Error executing command: " + e.getMessage());
        }
    }

    private void sendError(PrintWriter writer, String message) throws JsonProcessingException {
        Response response = new Response(ResponseStatus.ERROR, message);
        writer.println(JsonUtils.toJson(response));
    }

    @Override
    public void run(){
        try{
            while(true){
                System.out.println("Wait command from client...");
                Request request = JsonUtils.fromJson(reader.readLine(), Request.class);
                System.out.println("Server received command from client: " + request.toString());

                Method handler = commandMap.get(request.getType().name());

                if (handler != null) {
                    invokeHandler(handler, writer, request);
                } else {
                    sendError(writer, "Unknown command: " + request.getType());
                }

            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /*private void removeCustomer(Request request) throws JsonProcessingException {
        Integer removeId = JsonUtils.fromJson(request.getBody(), Integer.class);
        customerRepository.delete(customerRepository.findById(removeId));
        Response response = new Response(ResponceStatus.Ok, "");
        writer.println(JsonUtils.toJson(response));
    }

    private void updateCustomer(Request request) throws JsonProcessingException {
        Customer customer = JsonUtils.fromJson(request.getBody(), Customer.class);
        customerRepository.update(customer);
        Response response = new Response(ResponceStatus.Ok,"");
        writer.println(JsonUtils.toJson(response));
    }

    private void addCustomer(Request request) throws JsonProcessingException {
        Customer customer = JsonUtils.fromJson(request.getBody(), Customer.class);
        customerRepository.save(customer);
        Response response = new Response(ResponceStatus.Ok, "");
        writer.println(JsonUtils.toJson(response));
    }

    private void getCustomers() throws JsonProcessingException {
        Response response;
        List<Customer> customers = customerRepository.findAll();
        response = new Response(ResponceStatus.Ok, JsonUtils.toJson(customers));
        writer.println(JsonUtils.toJson(response));
    }

    private void addSupplier(Request request) throws JsonProcessingException {
        Supplier supplier = JsonUtils.fromJson(request.getBody(), Supplier.class);
        supplierRepository.save(supplier);
        Response response = new Response(ResponceStatus.Ok, "");
        writer.println(JsonUtils.toJson(response));
    }

    private void removeSupplier(Request request) throws JsonProcessingException {
        Integer removeId = JsonUtils.fromJson(request.getBody(), Integer.class);
        supplierRepository.delete(supplierRepository.findById(removeId));
        Response response = new Response(ResponceStatus.Ok, "");
        writer.println(JsonUtils.toJson(response));
    }

    private void updateSupplier(Request request) throws JsonProcessingException {
        Supplier supplier = JsonUtils.fromJson(request.getBody(), Supplier.class);
        supplierRepository.update(supplier);
        Response response = new Response(ResponceStatus.Ok,"");
        writer.println(JsonUtils.toJson(response));
    }

    private void getSuppliers() throws JsonProcessingException {
        Response response;
        List<Supplier> suppliers = supplierRepository.findAll();
        response = new Response(ResponceStatus.Ok, JsonUtils.toJson(suppliers));
        writer.println(JsonUtils.toJson(response));
    }

    private void removeCategory(Request request) throws JsonProcessingException {
        Integer removeId = JsonUtils.fromJson(request.getBody(), Integer.class);
        categoryRepository.delete(categoryRepository.findById(removeId));
        Response response = new Response(ResponceStatus.Ok, "");
        writer.println(JsonUtils.toJson(response));
    }

    private void updateCategory(Request request) throws JsonProcessingException {
        Category category = JsonUtils.fromJson(request.getBody(), Category.class);
        categoryRepository.update(category);
        Response response = new Response(ResponceStatus.Ok,"");
        writer.println(JsonUtils.toJson(response));
    }

    private void addCategory(Request request) throws JsonProcessingException {
        Category category = JsonUtils.fromJson(request.getBody(), Category.class);
        categoryRepository.save(category);
        Response response = new Response(ResponceStatus.Ok, "");
        writer.println(JsonUtils.toJson(response));
    }

    private void authorization(Request request) throws IOException {
        Response response;
        try {
            AuthorizationForm form = JsonUtils.fromJson(request.getBody(), AuthorizationForm.class);
            User user = userRepository.getByLogin(form.login);
            if (user == null) {
                response = new Response(ResponceStatus.INVALID_LOGIN, "");
            }else if(HeshUtils.hashSHA512(form.password).equals(user.getPasswordHash())){
                response = new Response(ResponceStatus.Ok, JsonUtils.toJson(new AuthorizationResult(user.getId(), user.getRole())));
            }else{
                response = new Response(ResponceStatus.INVALID_PASSWORD, "");
            }
        }catch (Exception e){
            e.printStackTrace();
            response = new Response(ResponceStatus.ERROR, "");
        }
        writer.println(JsonUtils.toJson(response));
    }

    private void getCategories() throws IOException {
        Response response;
        List<Category> categories = categoryRepository.findAll();
        response = new Response(ResponceStatus.Ok, JsonUtils.toJson(categories));
        writer.println(JsonUtils.toJson(response));
    }*/
}
