package com.dennymathew.commercebackend.dto;

public record ProductResponse(
        Long id,
        String name,
        Double price
) {
}
