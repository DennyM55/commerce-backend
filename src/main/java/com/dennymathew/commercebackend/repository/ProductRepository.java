
package com.dennymathew.commercebackend.repository;

import com.dennymathew.commercebackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.repository.query.Param;
public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    @Query("""
        SELECT p
        FROM Product p
        LEFT JOIN FETCH p.category
        """)
    List<Product> findAllWithCategory();


    @Query("SELECT p FROM Product p WHERE p.price > :price")
    List<Product> findProductsAbovePrice(
            @Param("price") Double price);

    List<Product> findByNameContainingIgnoreCase(String name);
}
