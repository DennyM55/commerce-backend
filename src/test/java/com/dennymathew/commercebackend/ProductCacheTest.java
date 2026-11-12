package com.dennymathew.commercebackend;

import com.dennymathew.commercebackend.entity.Product;
import com.dennymathew.commercebackend.repository.ProductRepository;
import com.dennymathew.commercebackend.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import javax.sql.DataSource;

import com.dennymathew.commercebackend.dto.ProductResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class ProductCacheTest {
    private Long testProductId;
    @Autowired
    private ProductService productService;
    @MockitoSpyBean
    private ProductRepository productRepository;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void shouldHaveProductCacheConfigured() {
        assertNotNull(cacheManager.getCache("products-v2"));
    }

    @Test
    void shouldUseTestDatabase() throws Exception {

        try (var connection = dataSource.getConnection()) {

            assertEquals(
                    "commerce_test_db",
                    connection.getCatalog()
            );
        }
    }

    @Test
    void shouldConnectToTestRedis() {

        try (var connection = redisConnectionFactory.getConnection()) {
            assertEquals("PONG", connection.ping());
        }
    }

    @Test
    void shouldUseTestRedisPort() {

        LettuceConnectionFactory factory =
                (LettuceConnectionFactory) redisConnectionFactory;

        assertEquals(6382, factory.getPort());
    }

    private Product createTestProduct() {

        Product product = new Product();
        product.setName("Test Laptop");
        product.setPrice(50000.0);

        Product saved = productRepository.save(product);
        testProductId = saved.getId();
        return saved;
    }

    @Test
    void shouldCacheProductAfterFirstLookup() {

        Product product = createTestProduct();
        Long id = product.getId();

        cacheManager.getCache("products-v2").evict(id);

        System.out.println(
                "Cache implementation: " +
                        cacheManager.getCache("products-v2").getClass().getName()
        );

        productService.getProductResponseById(id);
        productService.getProductResponseById(id);

        verify(productRepository, times(1)).findById(id);
    }

    @Test
    void shouldEvictCacheWhenProductIsUpdated() {

        Product product = createTestProduct();
        Long id = product.getId();

        productService.getProductResponseById(id);

        Product updated = new Product();
        updated.setName("Updated Laptop");
        updated.setPrice(60000.0);

        productService.updateProduct(id, updated, null);

        ProductResponse response =
                productService.getProductResponseById(id);

        assertEquals("Updated Laptop", response.name());
        assertEquals(60000.0, response.price());
    }
    @AfterEach
    void cleanup() {
        if (testProductId != null) {
            cacheManager.getCache("products-v2").evict(testProductId);
            productRepository.deleteById(testProductId);
            testProductId = null;
        }
    }
}
