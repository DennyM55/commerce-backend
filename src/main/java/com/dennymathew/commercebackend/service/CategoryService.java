
package com.dennymathew.commercebackend.service;

import com.dennymathew.commercebackend.entity.Category;
import com.dennymathew.commercebackend.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public Category create(String name) {
        String normalizedName = name.trim();

        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category already exists"
            );
        }

        Category category = new Category();
        category.setName(normalizedName);

        return repository.save(category);
    }

    public Category getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));
    }

    public List<Category> getAll() {
        return repository.findAll();
    }
}
