package org.com.techsalesmanagerserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.*;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;
import org.com.techsalesmanagerserver.model.OrderItem;
import org.com.techsalesmanagerserver.repository.OrderItemRepository;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public Response findAll() throws JsonProcessingException {
        log.info("Fetching all order items");
        List<OrderItem> orderItems = orderItemRepository.findAll();
        List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(orderItemDTOs));
    }

    public Response findById(Long id) throws JsonProcessingException {
        log.info("Fetching order item with id: {}", id);
        Optional<OrderItem> orderItemOptional = orderItemRepository.findById(id);

        Response response = new Response();
        if (orderItemOptional.isPresent()) {
            OrderItemDTO orderItemDTO = convertToDTO(orderItemOptional.get());
            response.setStatus(ResponseStatus.Ok);
            response.setBody(JsonUtils.toJson(orderItemDTO));
        } else {
            response.setStatus(ResponseStatus.ERROR);
            response.setBody("Элемент заказа не найден");
        }

        return response;
    }

    @Transactional
    public Response save(Request saveRequest) throws JsonProcessingException {
        OrderItemDTO orderItemDTO = JsonUtils.fromJson(saveRequest.getBody(), OrderItemDTO.class);
        log.info("Saving order item: {}", orderItemDTO);
        OrderItem orderItem = convertToEntity(orderItemDTO);
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        OrderItemDTO savedOrderItemDTO = convertToDTO(savedOrderItem);
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(savedOrderItemDTO));
    }

    @Transactional
    public Response deleteById(Request deleteRequest) throws JsonProcessingException {
        Long id = JsonUtils.fromJson(deleteRequest.getBody(), Long.class);
        log.info("Deleting order item with id: {}", id);

        if (orderItemRepository.existsById(id)) {
            orderItemRepository.deleteById(id);
            return new Response(ResponseStatus.Ok, "Элемент заказа успешно удален");
        } else {
            return new Response(ResponseStatus.ERROR, "Элемент заказа не найден");
        }
    }

    @Transactional
    public Response update(Request updateRequest) throws JsonProcessingException {
        OrderItemDTO orderItemDTO = JsonUtils.fromJson(updateRequest.getBody(), OrderItemDTO.class);
        log.info("Updating order item: {}", orderItemDTO);
        if (orderItemRepository.findById(orderItemDTO.getId()).isPresent()) {
            OrderItem orderItem = convertToEntity(orderItemDTO);
            OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
            OrderItemDTO updatedOrderItemDTO = convertToDTO(updatedOrderItem);
            log.info("Order item updated: {}", updatedOrderItemDTO);
            return new Response(ResponseStatus.Ok, JsonUtils.toJson(updatedOrderItemDTO));
        } else {
            log.info("Order item not found");
            return new Response(ResponseStatus.ERROR, "Элемент заказа не найден");
        }
    }

    private OrderItemDTO convertToDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
                .productName(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice().doubleValue())
                .build();
    }

    private OrderItem convertToEntity(OrderItemDTO orderItemDTO) {
        return OrderItem.builder()
                .id(orderItemDTO.getId())
                .productName(orderItemDTO.getProductName())
                .quantity(orderItemDTO.getQuantity())
                .price(BigDecimal.valueOf(orderItemDTO.getPrice()))
                .build();
    }
} 