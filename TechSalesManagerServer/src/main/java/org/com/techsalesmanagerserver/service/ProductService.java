package org.com.techsalesmanagerserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.*;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;
import org.com.techsalesmanagerserver.model.Product;
import org.com.techsalesmanagerserver.repository.ProductRepository;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Response findAll() throws JsonProcessingException {
        log.info("Fetching all products");
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(productDTOs));
    }

    public Response findById(Long id) throws JsonProcessingException {
        log.info("Fetching product with id: {}", id);
        Optional<Product> productOptional = productRepository.findById(id);

        Response response = new Response();
        if (productOptional.isPresent()) {
            ProductDTO productDTO = convertToDTO(productOptional.get());
            response.setStatus(ResponseStatus.Ok);
            response.setBody(JsonUtils.toJson(productDTO));
        } else {
            response.setStatus(ResponseStatus.ERROR);
            response.setBody("Товар не найден");
        }

        return response;
    }

    @Transactional
    public Response save(Request saveRequest) throws JsonProcessingException {
        ProductDTO productDTO = JsonUtils.fromJson(saveRequest.getBody(), ProductDTO.class);
        log.info("Saving product: {}", productDTO);
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        ProductDTO savedProductDTO = convertToDTO(savedProduct);
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(savedProductDTO));
    }

    @Transactional
    public Response deleteById(Request deleteRequest) throws JsonProcessingException {
        Long id = JsonUtils.fromJson(deleteRequest.getBody(), Long.class);
        log.info("Deleting product with id: {}", id);

        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return new Response(ResponseStatus.Ok, "Товар успешно удален");
        } else {
            return new Response(ResponseStatus.ERROR, "Товар не найден");
        }
    }

    @Transactional
    public Response update(Request updateRequest) throws JsonProcessingException {
        ProductDTO productDTO = JsonUtils.fromJson(updateRequest.getBody(), ProductDTO.class);
        log.info("Updating product: {}", productDTO);
        if (productRepository.findById(productDTO.getId()).isPresent()) {
            Product product = convertToEntity(productDTO);
            Product updatedProduct = productRepository.save(product);
            ProductDTO updatedProductDTO = convertToDTO(updatedProduct);
            log.info("Product updated: {}", updatedProductDTO);
            return new Response(ResponseStatus.Ok, JsonUtils.toJson(updatedProductDTO));
        } else {
            log.info("Product not found");
            return new Response(ResponseStatus.ERROR, "Товар не найден");
        }
    }

    public Response findByName(String name) {
        try {
            log.info("Finding product by name: {}", name);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(productRepository.findByName(name)))
                    .build();
        } catch (Exception e) {
            log.error("Error finding product by name: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }

    public Response findByCategory(Long categoryId) {
        try {
            log.info("Finding products by category: {}", categoryId);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(productRepository.findByCategoryId(categoryId)))
                    .build();
        } catch (Exception e) {
            log.error("Error finding products by category: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }

    public Response findBySupplier(Long supplierId) {
        try {
            log.info("Finding products by supplier: {}", supplierId);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(productRepository.findBySupplierId(supplierId)))
                    .build();
        } catch (Exception e) {
            log.error("Error finding products by supplier: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }

    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .build();
    }

    private Product convertToEntity(ProductDTO productDTO) {
        return Product.builder()
                .id(productDTO.getId())
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .stock(productDTO.getStock())
                .build();
    }
} 