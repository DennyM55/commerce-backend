
package com.dennymathew.commercebackend.controller;

import com.dennymathew.commercebackend.dto.CategoryRequest;
import com.dennymathew.commercebackend.dto.CategoryResponse;
import com.dennymathew.commercebackend.entity.Category;
import com.dennymathew.commercebackend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Invalid category data"),
            @ApiResponse(responseCode = "409", description = "Category already exists")
    })
    public CategoryResponse create(
            @Valid @RequestBody CategoryRequest request) {
        return toResponse(categoryService.create(request.name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public CategoryResponse getById(@PathVariable Long id) {
        return toResponse(categoryService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all categories")
    @ApiResponse(responseCode = "200", description = "Categories returned")
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
