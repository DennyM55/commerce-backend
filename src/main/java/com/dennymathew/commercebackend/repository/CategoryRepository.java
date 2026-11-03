
package com.dennymathew.commercebackend.repository;

import com.dennymathew.commercebackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);
}
