
package com.dennymathew.commercebackend.service.impl;
import org.springframework.cache.annotation.Cacheable;
import com.dennymathew.commercebackend.dto.ProductDetailResponse;
import com.dennymathew.commercebackend.entity.Category;
import com.dennymathew.commercebackend.entity.Product;
import com.dennymathew.commercebackend.exception.ProductNotFoundException;
import com.dennymathew.commercebackend.repository.CategoryRepository;
import com.dennymathew.commercebackend.repository.ProductRepository;
import com.dennymathew.commercebackend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(
            ProductRepository repository,
            CategoryRepository categoryRepository) {

        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product save(Product product, Long categoryId) {

        if (categoryId != null) {
            product.setCategory(findCategory(categoryId));
        }

        return repository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Product getProductByID(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id));
    }

    @Override
    public Product updateProduct(
            Long id,
            Product product,
            Long categoryId) {

        Product existing = getProductByID(id);

        existing.setName(product.getName());
        existing.setPrice(product.getPrice());

        if (categoryId != null) {
            existing.setCategory(findCategory(categoryId));
        }

        return repository.save(existing);
    }

    @Override
    public void deleteProduct(Long id) {
        Product existing = getProductByID(id);
        repository.delete(existing);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found: " + id
                        ));
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        findCategory(categoryId);
        return repository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> getAllProductsWithCategory() {
        return repository.findAllWithCategory();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailResponse> getProductDetailsLazy() {

        return repository.findAll()
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


    @Override
    public List<Product> getProductsAbovePrice(Double price) {
        return repository.findProductsAbovePrice(price);
    }

    @Override
    public Page<Product> getProducts(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public List<Product> searchProducts(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Page<Product> searchProducts(
            String name,
            Pageable pageable) {

        return repository.findByNameContainingIgnoreCase(
                name, pageable);
    }
}
