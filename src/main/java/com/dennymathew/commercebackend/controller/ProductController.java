package com.dennymathew.commercebackend.controller;

import com.dennymathew.commercebackend.entity.Product;
import com.dennymathew.commercebackend.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //accepts a Product in the request body and returns the saved product.
    @PostMapping
    public Product saveProduct(@RequestBody Product product) {
        // Call the service layer to save the product
        return productService.save(product);
    }

    @GetMapping
    public List<Product> getAllProducts() {
        // Call the service layer to get all products
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductByID(@PathVariable Long id) {
        return productService.getProductByID(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }
}
