
package com.dennymathew.commercebackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Product creation or update request")
public record ProductRequest(
        @Schema(description = "Product name", example = "Laptop")
        @NotBlank String name,
        @Schema(description = "Product price", example = "999.99")
        @NotNull @Positive Double price,
        @Schema(description = "Associated category ID", example = "1", nullable = true)
        Long categoryId
) {
}
