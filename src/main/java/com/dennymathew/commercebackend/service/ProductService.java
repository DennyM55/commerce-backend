
package com.dennymathew.commercebackend.service;

import com.dennymathew.commercebackend.dto.ProductDetailResponse;
import com.dennymathew.commercebackend.entity.Product;

import java.util.List;

public interface ProductService {

    Product save(Product product, Long categoryId);

    List<Product> getAllProducts();

    Product getProductByID(Long id);

    Product updateProduct(
            Long id,
            Product product,
            Long categoryId
    );

    void deleteProduct(Long id);
    List<Product> getProductsByCategory(Long categoryId);
    List<Product> getAllProductsWithCategory();
    List<ProductDetailResponse> getProductDetailsLazy();
    List<Product> getProductsAbovePrice(Double price);
}
