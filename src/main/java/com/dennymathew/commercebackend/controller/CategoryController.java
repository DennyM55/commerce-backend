
package com.dennymathew.commercebackend.controller;

import com.dennymathew.commercebackend.dto.CategoryRequest;
import com.dennymathew.commercebackend.dto.CategoryResponse;
import com.dennymathew.commercebackend.entity.Category;
import com.dennymathew.commercebackend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
            @Valid @RequestBody CategoryRequest request) {
        return toResponse(categoryService.create(request.name()));
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable Long id) {
        return toResponse(categoryService.getById(id));
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}
