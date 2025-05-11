package org.com.techsalesmanagerserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerserver.dto.*;
import org.com.techsalesmanagerserver.enumeration.ResponseStatus;
import org.com.techsalesmanagerserver.model.Category;
import org.com.techsalesmanagerserver.repository.CategoryRepository;
import org.com.techsalesmanagerserver.server.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Response findAll() throws JsonProcessingException {
        log.info("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(categoryDTOs));
    }

    public Response findById(Long id) throws JsonProcessingException {
        log.info("Fetching category with id: {}", id);
        Optional<Category> categoryOptional = categoryRepository.findById(id);

        Response response = new Response();
        if (categoryOptional.isPresent()) {
            CategoryDTO categoryDTO = convertToDTO(categoryOptional.get());
            response.setStatus(ResponseStatus.Ok);
            response.setBody(JsonUtils.toJson(categoryDTO));
        } else {
            response.setStatus(ResponseStatus.ERROR);
            response.setBody("Категория не найдена");
        }

        return response;
    }

    @Transactional
    public Response save(Request saveRequest) throws JsonProcessingException {
        CategoryDTO categoryDTO = JsonUtils.fromJson(saveRequest.getBody(), CategoryDTO.class);
        log.info("Saving category: {}", categoryDTO);
        Category category = convertToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        CategoryDTO savedCategoryDTO = convertToDTO(savedCategory);
        return new Response(ResponseStatus.Ok, JsonUtils.toJson(savedCategoryDTO));
    }

    @Transactional
    public Response deleteById(Request deleteRequest) throws JsonProcessingException {
        Long id = JsonUtils.fromJson(deleteRequest.getBody(), Long.class);
        log.info("Deleting category with id: {}", id);

        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return new Response(ResponseStatus.Ok, "Категория успешно удалена");
        } else {
            return new Response(ResponseStatus.ERROR, "Категория не найдена");
        }
    }

    @Transactional
    public Response update(Request updateRequest) throws JsonProcessingException {
        CategoryDTO categoryDTO = JsonUtils.fromJson(updateRequest.getBody(), CategoryDTO.class);
        log.info("Updating category: {}", categoryDTO);
        if (categoryRepository.findById(categoryDTO.getId()).isPresent()) {
            Category category = convertToEntity(categoryDTO);
            Category updatedCategory = categoryRepository.save(category);
            CategoryDTO updatedCategoryDTO = convertToDTO(updatedCategory);
            log.info("Category updated: {}", updatedCategoryDTO);
            return new Response(ResponseStatus.Ok, JsonUtils.toJson(updatedCategoryDTO));
        } else {
            log.info("Category not found");
            return new Response(ResponseStatus.ERROR, "Категория не найдена");
        }
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    private Category convertToEntity(CategoryDTO categoryDTO) {
        return Category.builder()
                .id(categoryDTO.getId())
                .name(categoryDTO.getName())
                .build();
    }

    public Response findByName(String name) {
        try {
            log.info("Finding category by name: {}", name);
            return Response.builder()
                    .status(ResponseStatus.Ok)
                    .body(JsonUtils.toJson(categoryRepository.findByName(name)))
                    .build();
        } catch (Exception e) {
            log.error("Error finding category by name: {}", e.getMessage());
            return Response.builder()
                    .status(ResponseStatus.ERROR)
                    .build();
        }
    }
}