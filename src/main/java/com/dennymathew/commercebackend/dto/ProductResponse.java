package com.dennymathew.commercebackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "Product response")
public record ProductResponse(
        @Schema(description = "Product ID", example = "10") Long id,
        @Schema(description = "Product name", example = "Laptop") String name,
        @Schema(description = "Product price", example = "999.99") Double price,
        @Schema(description = "Associated category ID", example = "1", nullable = true) Long categoryId
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
