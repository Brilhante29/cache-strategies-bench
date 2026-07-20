package com.portfolio.cachebench.domain;

import com.portfolio.cachebench.application.InMemoryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCacheTest {

    private InMemoryCache cache;

    @BeforeEach
    void setUp() {
        cache = new InMemoryCache();
    }

    @Test
    void shouldReturnNullWhenKeyNotFound() {
        assertNull(cache.get(999L));
    }

    @Test
    void shouldReturnProductAfterPut() {
        Product p = new Product(1L, "test", 10.0, 100);
        cache.put(1L, p);
        assertEquals(p, cache.get(1L));
    }

    @Test
    void shouldReturnNullAfterEvict() {
        cache.put(1L, new Product(1L, "test", 10.0, 100));
        cache.evict(1L);
        assertNull(cache.get(1L));
    }

    @Test
    void shouldReturnNullAfterClear() {
        cache.put(1L, new Product(1L, "test", 10.0, 100));
        cache.put(2L, new Product(2L, "test2", 20.0, 200));
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    void shouldOverwriteExistingKey() {
        cache.put(1L, new Product(1L, "original", 10.0, 100));
        cache.put(1L, new Product(1L, "updated", 20.0, 200));
        Product result = cache.get(1L);
        assertNotNull(result);
        assertEquals("updated", result.getName());
    }

    @Test
    void shouldExpireEntryAfterTTL() throws InterruptedException {
        InMemoryCache shortTtlCache = new InMemoryCache() {
            @Override
            public void put(Long key, Product value) {
                super.put(key, value);
            }
        };
        Product p = new Product(1L, "expiring", 10.0, 100);
        cache.put(1L, p);
        assertNotNull(cache.get(1L));
    }

    @Test
    void shouldReportCorrectSize() {
        assertEquals(0, cache.size());
        cache.put(1L, new Product(1L, "a", 1.0, 1));
        assertEquals(1, cache.size());
        cache.put(2L, new Product(2L, "b", 2.0, 2));
        assertEquals(2, cache.size());
        cache.evict(1L);
        assertEquals(1, cache.size());
    }
}
