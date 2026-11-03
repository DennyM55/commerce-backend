
package com.dennymathew.commercebackend.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name
) {
}
