package com.dennymathew.commercebackend;

import com.dennymathew.commercebackend.entity.Product;
import com.dennymathew.commercebackend.exception.ProductNotFoundException;
import com.dennymathew.commercebackend.repository.ProductRepository;
import com.dennymathew.commercebackend.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import com.dennymathew.commercebackend.dto.ProductResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ProductCacheTest {
    private static final long CACHE_WAIT_MILLIS = 2_000;
    private final List<Long> testProductIds = new ArrayList<>();
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearProductCache() {
        cacheManager.getCache("products-v2").clear();
    }

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
        testProductIds.add(saved.getId());
        return saved;
    }

    private void awaitCacheState(Long id, boolean shouldBePresent)
            throws InterruptedException {
        long deadline = System.nanoTime() + CACHE_WAIT_MILLIS * 1_000_000;

        while ((cacheManager.getCache("products-v2").get(id) != null)
                != shouldBePresent && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }

        if (shouldBePresent) {
            assertNotNull(cacheManager.getCache("products-v2").get(id));
        } else {
            assertNull(cacheManager.getCache("products-v2").get(id));
        }
    }

    @Test
    void shouldCacheProductAfterFirstLookup() {

        Product product = createTestProduct();
        Long id = product.getId();

        ProductResponse firstResponse = productService.getProductResponseById(id);
        ProductResponse cacheHit = productService.getProductResponseById(id);
        assertEquals(firstResponse, cacheHit);

        jdbcTemplate.update("DELETE FROM product WHERE id = ?", id);

        ProductResponse cachedResponse = productService.getProductResponseById(id);
        assertEquals(firstResponse, cachedResponse);
    }

    @Test
    void shouldEvictOnlyUpdatedProduct() throws InterruptedException {

        Product product10 = createTestProduct();
        Product product11 = createTestProduct();
        Long product10Id = product10.getId();
        Long product11Id = product11.getId();

        productService.getProductResponseById(product10Id);
        productService.getProductResponseById(product11Id);
        awaitCacheState(product10Id, true);
        awaitCacheState(product11Id, true);

        Product updated = new Product();
        updated.setName("Updated Laptop");
        updated.setPrice(60000.0);

        productService.updateProduct(product10Id, updated, null);

        awaitCacheState(product10Id, false);
        assertNotNull(cacheManager.getCache("products-v2").get(product11Id));

        ProductResponse response = productService.getProductResponseById(product10Id);

        assertEquals("Updated Laptop", response.name());
        assertEquals(60000.0, response.price());
    }

    @Test
    void shouldEvictOnlyDeletedProduct() throws InterruptedException {

        Product product10 = createTestProduct();
        Product product11 = createTestProduct();
        Long product10Id = product10.getId();
        Long product11Id = product11.getId();

        productService.getProductResponseById(product10Id);
        ProductResponse product11Response =
                productService.getProductResponseById(product11Id);
        awaitCacheState(product10Id, true);
        awaitCacheState(product11Id, true);

        productService.deleteProduct(product10Id);

        awaitCacheState(product10Id, false);
        assertNotNull(cacheManager.getCache("products-v2").get(product11Id));
        assertEquals(
                product11Response,
                productService.getProductResponseById(product11Id)
        );

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductResponseById(product10Id)
        );
    }

    @Test
    void shouldFailWhenReadingDeletedProduct() throws InterruptedException {

        Product product = createTestProduct();
        Long id = product.getId();

        productService.getProductResponseById(id);
        awaitCacheState(id, true);
        productService.deleteProduct(id);
        awaitCacheState(id, false);

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductResponseById(id)
        );
    }

    @AfterEach
    void cleanup() {
        cacheManager.getCache("products-v2").clear();
        for (Long id : testProductIds) {
            jdbcTemplate.update("DELETE FROM product WHERE id = ?", id);
        }
        testProductIds.clear();
    }
}
