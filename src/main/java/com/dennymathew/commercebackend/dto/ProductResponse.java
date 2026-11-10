package com.dennymathew.commercebackend.dto;

import java.io.Serializable;

public record ProductResponse(
        Long id,
        String name,
        Double price,
        Long categoryId
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
