
package com.dennymathew.commercebackend.repository;

import com.dennymathew.commercebackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    @Query("""
        SELECT p
        FROM Product p
        LEFT JOIN FETCH p.category
        """)
    List<Product> findAllWithCategory();
}
