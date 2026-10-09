
package com.dennymathew.commercebackend.dto;

public record ProductDetailResponse(
        Long id,
        String name,
        Double price,
        Long categoryId,
        String categoryName
) {
}
