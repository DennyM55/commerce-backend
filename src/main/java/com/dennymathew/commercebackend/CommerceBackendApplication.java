package com.dennymathew.commercebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CommerceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommerceBackendApplication.class, args);
    }

}
