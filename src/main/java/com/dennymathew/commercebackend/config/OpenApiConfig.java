package com.dennymathew.commercebackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Commerce Backend API",
                version = "1.0.0",
                description = "E-commerce backend REST APIs."
        )
)
public class OpenApiConfig {
}
