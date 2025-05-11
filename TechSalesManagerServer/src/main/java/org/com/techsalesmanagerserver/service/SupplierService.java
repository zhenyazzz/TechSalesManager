package org.com.techsalesmanagerserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.*;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;
import org.com.techsalesmanagerserver.model.Product;
import org.com.techsalesmanagerserver.model.Supplier;
import org.com.techsalesmanagerserver.repository.ProductRepository;
import org.com.techsalesmanagerserver.repository.SupplierRepository;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public Response findAll() throws JsonProcessingException {
        log.info("Fetching all suppliers");
        List<Supplier> suppliers = supplierRepository.findAll();
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(supplierDTOs));
    }

    public Response findById(Long id) throws JsonProcessingException {
        log.info("Fetching supplier with id: {}", id);
        Optional<Supplier> supplierOptional = supplierRepository.findById(id);

        Response response = new Response();
        if (supplierOptional.isPresent()) {
            SupplierDTO supplierDTO = convertToDTO(supplierOptional.get());
            response.setStatus(ResponseStatus.Ok);
            response.setBody(JsonUtils.toJson(supplierDTO));
        } else {
            response.setStatus(ResponseStatus.ERROR);
            response.setBody("Поставщик не найден");
        }

        return response;
    }

    @Transactional
    public Response save(Request saveRequest) throws JsonProcessingException {
        SupplierDTO supplierDTO = JsonUtils.fromJson(saveRequest.getBody(), SupplierDTO.class);
        log.info("Saving supplier: {}", supplierDTO);
        Supplier supplier = convertToEntity(supplierDTO);
        Supplier savedSupplier = supplierRepository.save(supplier);
        SupplierDTO savedSupplierDTO = convertToDTO(savedSupplier);
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(savedSupplierDTO));
    }

    @Transactional
    public Response deleteById(Request deleteRequest) throws JsonProcessingException {
        Long id = JsonUtils.fromJson(deleteRequest.getBody(), Long.class);
        log.info("Deleting supplier with id: {}", id);

        if (supplierRepository.existsById(id)) {
            supplierRepository.deleteById(id);
            return new Response(ResponseStatus.Ok, "Поставщик успешно удален");
        } else {
            return new Response(ResponseStatus.ERROR, "Поставщик не найден");
        }
    }

    @Transactional
    public Response update(Request updateRequest) throws JsonProcessingException {
        SupplierDTO supplierDTO = JsonUtils.fromJson(updateRequest.getBody(), SupplierDTO.class);
        log.info("Updating supplier: {}", supplierDTO);
        if (supplierRepository.findById(supplierDTO.getId()).isPresent()) {
            Supplier supplier = convertToEntity(supplierDTO);
            Supplier updatedSupplier = supplierRepository.save(supplier);
            SupplierDTO updatedSupplierDTO = convertToDTO(updatedSupplier);
            log.info("Supplier updated: {}", updatedSupplierDTO);
            return new Response(ResponseStatus.Ok, JsonUtils.toJson(updatedSupplierDTO));
        } else {
            log.info("Supplier not found");
            return new Response(ResponseStatus.ERROR, "Поставщик не найден");
        }
    }

    @Transactional
    public Response addProductToSupplier(Request request) throws JsonProcessingException {
        SupplierProductDTO supplierProductDTO = JsonUtils.fromJson(request.getBody(), SupplierProductDTO.class);
        log.info("Adding product {} to supplier {}", supplierProductDTO.getProductId(), supplierProductDTO.getSupplierId());

        Optional<Supplier> supplierOptional = supplierRepository.findById(supplierProductDTO.getSupplierId());
        Optional<Product> productOptional = productRepository.findById(supplierProductDTO.getProductId());

        if (supplierOptional.isEmpty()) {
            return new Response(ResponseStatus.ERROR, "Поставщик не найден");
        }

        if (productOptional.isEmpty()) {
            return new Response(ResponseStatus.ERROR, "Товар не найден");
        }

        Supplier supplier = supplierOptional.get();
        Product product = productOptional.get();

        // Проверяем, не связан ли уже товар с другим поставщиком
        if (product.getSupplier() != null && !product.getSupplier().getId().equals(supplier.getId())) {
            return new Response(ResponseStatus.ERROR, "Товар уже связан с другим поставщиком");
        }

        // Устанавливаем связь
        product.setSupplier(supplier);
        productRepository.save(product);

        // Возвращаем обновленную информацию о поставщике
        SupplierDTO supplierDTO = convertToDTO(supplier);
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(supplierDTO));
    }

    @Transactional
    public Response removeProductFromSupplier(Request request) throws JsonProcessingException {
        SupplierProductDTO supplierProductDTO = JsonUtils.fromJson(request.getBody(), SupplierProductDTO.class);
        log.info("Removing product {} from supplier {}", supplierProductDTO.getProductId(), supplierProductDTO.getSupplierId());

        Optional<Product> productOptional = productRepository.findById(supplierProductDTO.getProductId());

        if (productOptional.isEmpty()) {
            return new Response(ResponseStatus.ERROR, "Товар не найден");
        }

        Product product = productOptional.get();

        // Проверяем, связан ли товар с указанным поставщиком
        if (product.getSupplier() == null || !product.getSupplier().getId().equals(supplierProductDTO.getSupplierId())) {
            return new Response(ResponseStatus.ERROR, "Товар не связан с указанным поставщиком");
        }

        // Удаляем связь
        product.setSupplier(null);
        productRepository.save(product);

        return new Response(ResponseStatus.Ok, "Товар успешно отвязан от поставщика");
    }

    public Response getSupplierProducts(Long supplierId) throws JsonProcessingException {
        log.info("Getting products for supplier {}", supplierId);

        Optional<Supplier> supplierOptional = supplierRepository.findById(supplierId);
        if (supplierOptional.isEmpty()) {
            return new Response(ResponseStatus.ERROR, "Поставщик не найден");
        }

        Supplier supplier = supplierOptional.get();
        List<Product> products = supplier.getProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> ProductDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                        .supplierId(supplierId)
                        .build())
                .collect(Collectors.toList());

        return new Response(ResponseStatus.Ok, JsonUtils.toJson(productDTOs));
    }

    public Response findByName(String name) {
        try {
            log.info("Finding supplier by name: {}", name);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(supplierRepository.findByName(name)))
                    .build();
        } catch (Exception e) {
            log.error("Error finding supplier by name: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }

    public Response findByProduct(Long productId) {
        try {
            log.info("Finding supplier by product: {}", productId);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(supplierRepository.findByProductsId(productId)))
                    .build();
        } catch (Exception e) {
            log.error("Error finding supplier by product: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }

    private SupplierDTO convertToDTO(Supplier supplier) {
        return SupplierDTO.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactInfo(supplier.getContactInfo())
                .build();
    }

    private Supplier convertToEntity(SupplierDTO supplierDTO) {
        return Supplier.builder()
                .id(supplierDTO.getId())
                .name(supplierDTO.getName())
                .contactInfo(supplierDTO.getContactInfo())
                .build();
    }
} 