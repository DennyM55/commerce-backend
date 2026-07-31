package com.dennymathew.commercebackend.service;

import com.dennymathew.commercebackend.entity.Product;

import java.util.List;

public interface ProductService {

    Product save(Product product);

    List<Product> getAllProducts();

    Product getProductByID(Long id);
}