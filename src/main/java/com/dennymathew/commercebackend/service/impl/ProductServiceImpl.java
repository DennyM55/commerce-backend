package com.dennymathew.commercebackend.service.impl;

import com.dennymathew.commercebackend.entity.Product;
import com.dennymathew.commercebackend.exception.ProductNotFoundException;
import com.dennymathew.commercebackend.repository.ProductRepository;
import com.dennymathew.commercebackend.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        return repository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    @Override
    public Product getProductByID(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = getProductByID(id);
        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        return repository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product existingProduct = getProductByID(id);
        repository.delete(existingProduct);
    }

}