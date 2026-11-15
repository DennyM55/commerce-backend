
package com.dennymathew.commercebackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product response including category details")
public record ProductDetailResponse(
        @Schema(description = "Product ID", example = "10") Long id,
        @Schema(description = "Product name", example = "Laptop") String name,
        @Schema(description = "Product price", example = "999.99") Double price,
        @Schema(description = "Associated category ID", example = "1", nullable = true) Long categoryId,
        @Schema(description = "Associated category name", example = "Electronics", nullable = true) String categoryName
) {
}
