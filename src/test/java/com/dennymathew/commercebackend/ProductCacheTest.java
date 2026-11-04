package com.dennymathew.commercebackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ProductCacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldHaveProductCacheConfigured() {
        assertNotNull(cacheManager.getCache("products-v2"));
    }
}