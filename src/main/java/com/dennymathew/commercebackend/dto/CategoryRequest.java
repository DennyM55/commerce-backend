
package com.dennymathew.commercebackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Category creation request")
public record CategoryRequest(
        @Schema(description = "Category name", example = "Electronics")
        @NotBlank String name
) {
}
