package org.com.techsalesmanagerserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.*;
import org.com.techsalesmanagerserver.enumeration.OrderStatus;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;
import org.com.techsalesmanagerserver.model.Order;
import org.com.techsalesmanagerserver.repository.OrderRepository;
import org.com.techsalesmanagerserver.repository.UserRepository;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    public Response findAll() throws JsonProcessingException {
        log.info("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(orderDTOs));
    }

    public Response findById(Long id) throws JsonProcessingException {
        log.info("Fetching order with id: {}", id);
        Optional<Order> orderOptional = orderRepository.findById(id);

        Response response = new Response();
        if (orderOptional.isPresent()) {
            OrderDTO orderDTO = convertToDTO(orderOptional.get());
            response.setStatus(ResponseStatus.Ok);
            response.setBody(JsonUtils.toJson(orderDTO));
        } else {
            response.setStatus(ResponseStatus.ERROR);
            response.setBody("Заказ не найден");
        }

        return response;
    }

    @Transactional
    public Response save(Request saveRequest) throws JsonProcessingException {
        OrderDTO orderDTO = JsonUtils.fromJson(saveRequest.getBody(), OrderDTO.class);
        log.info("Saving order: {}", orderDTO);
        Order order = convertToEntity(orderDTO);
        Order savedOrder = orderRepository.save(order);
        OrderDTO savedOrderDTO = convertToDTO(savedOrder);
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(savedOrderDTO));
    }

    @Transactional
    public Response deleteById(Request deleteRequest) throws JsonProcessingException {
        Long id = JsonUtils.fromJson(deleteRequest.getBody(), Long.class);
        log.info("Deleting order with id: {}", id);

        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return new Response(ResponseStatus.Ok, "Заказ успешно удален");
        } else {
            return new Response(ResponseStatus.ERROR, "Заказ не найден");
        }
    }

    @Transactional
    public Response update(Request updateRequest) throws JsonProcessingException {
        OrderDTO orderDTO = JsonUtils.fromJson(updateRequest.getBody(), OrderDTO.class);
        log.info("Updating order: {}", orderDTO);
        if (orderRepository.findById(orderDTO.getId()).isPresent()) {
            Order order = convertToEntity(orderDTO);
            Order updatedOrder = orderRepository.save(order);
            OrderDTO updatedOrderDTO = convertToDTO(updatedOrder);
            log.info("Order updated: {}", updatedOrderDTO);
            return new Response(ResponseStatus.Ok, JsonUtils.toJson(updatedOrderDTO));
        } else {
            log.info("Order not found");
            return new Response(ResponseStatus.ERROR, "Заказ не найден");
        }
    }

    public Response findByUser(Long userId) {
        try {
            log.info("Finding orders by user: {}", userId);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(orderRepository.findByUserId(userId)))
                    .build();
        } catch (Exception e) {
            log.error("Error finding orders by user: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }

    public Response findByStatus(String status) {
        try {
            log.info("Finding orders by status: {}", status);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(orderRepository.findByStatus(OrderStatus.valueOf(status))))
                    .build();
        } catch (Exception e) {
            log.error("Error finding orders by status: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }

    /*public Response findByDateRange(String dateRange) {
        try {
            log.info("Finding orders by date range: {}", dateRange);
            String[] dates = dateRange.split(",");
            LocalDateTime startDate = LocalDateTime.parse(dates[0]);
            LocalDateTime endDate = LocalDateTime.parse(dates[1]);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(orderRepository.findByCreatedAtBetween(startDate, endDate)))
                    .build();
        } catch (Exception e) {
            log.error("Error finding orders by date range: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }*/

    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .customerName(order.getCustomerName())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount().doubleValue())
                .build();
    }

    private Order convertToEntity(OrderDTO orderDTO) {
        return Order.builder()
                .id(orderDTO.getId())
                .user(userRepository.findById(orderDTO.getUserId()).orElse(null))
                .customerName(orderDTO.getCustomerName())
                .orderDate(orderDTO.getOrderDate())
                .status(OrderStatus.valueOf(orderDTO.getStatus()))
                .totalAmount(BigDecimal.valueOf(orderDTO.getTotalAmount()))
                .build();
    }
} 