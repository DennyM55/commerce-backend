
package com.dennymathew.commercebackend.controller;

import com.dennymathew.commercebackend.dto.ProductDetailResponse;
import com.dennymathew.commercebackend.dto.ProductRequest;
import com.dennymathew.commercebackend.dto.ProductResponse;
import com.dennymathew.commercebackend.entity.Category;
import com.dennymathew.commercebackend.entity.Product;
import com.dennymathew.commercebackend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management and query APIs")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ProductResponse saveProduct(
            @Valid @RequestBody ProductRequest request) {

        Product saved = productService.save(
                toEntity(request),
                request.categoryId()
        );
        return toResponse(saved);
    }

    @GetMapping
    @Operation(summary = "List products with pagination")
    @ApiResponse(responseCode = "200", description = "Product page returned")
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productService.getProducts(pageable)
                .map(this::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse getProductByID(@PathVariable Long id) {
        return productService.getProductResponseById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "404", description = "Product or category not found")
    })
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        Product updated = productService.updateProduct(
                id,
                toEntity(request),
                request.categoryId()
        );
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    private Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        return product;
    }

    private ProductResponse toResponse(Product product) {

        Long categoryId = product.getCategory() == null
                ? null
                : product.getCategory().getId();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                categoryId
        );
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List products by category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products returned"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public List<ProductResponse> getProductsByCategory(
            @PathVariable Long categoryId) {

        return productService.getProductsByCategory(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/details")
    @Operation(summary = "List product details with categories")
    @ApiResponse(responseCode = "200", description = "Product details returned")
    public List<ProductDetailResponse> getAllProductDetails() {

        return productService.getAllProductsWithCategory()
                .stream()
                .map(product -> {

                    Category category = product.getCategory();

                    return new ProductDetailResponse(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            category == null ? null : category.getId(),
                            category == null ? null : category.getName()
                    );
                })
                .toList();
    }

    @GetMapping("/details-lazy")
    @Operation(summary = "List product details using lazy loading")
    @ApiResponse(responseCode = "200", description = "Product details returned")
    public List<ProductDetailResponse> getProductDetailsLazy() {
        return productService.getProductDetailsLazy();
    }


    @GetMapping("/above-price")
    @Operation(summary = "List products above a price")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products returned"),
            @ApiResponse(responseCode = "400", description = "Invalid price parameter")
    })
    public List<ProductResponse> getProductsAbovePrice(
            @RequestParam Double price) {

        return productService.getProductsAbovePrice(price)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public Page<ProductResponse> searchProducts(
            @RequestParam String name,
            Pageable pageable) {

        return productService.searchProducts(name, pageable)
                .map(this::toResponse);
    }


}
