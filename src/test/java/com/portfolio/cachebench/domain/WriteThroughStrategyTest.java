package com.portfolio.cachebench.domain;

import com.portfolio.cachebench.application.InMemoryCache;
import com.portfolio.cachebench.application.InMemoryProductStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WriteThroughStrategyTest {

    private InMemoryCache cache;
    private InMemoryProductStore store;
    private WriteThroughStrategy strategy;

    @BeforeEach
    void setUp() {
        cache = new InMemoryCache();
        store = new InMemoryProductStore();
        strategy = new WriteThroughStrategy(cache, store);
    }

    @Test
    void shouldPopulateCacheOnWrite() {
        Product saved = strategy.save(new Product(1L, "write-through", 10.0, 100));
        assertNotNull(cache.get(1L));
        assertEquals("write-through", cache.get(1L).getName());
    }

    @Test
    void shouldReadFromCacheAfterWrite() {
        strategy.save(new Product(1L, "cached", 10.0, 100));
        strategy.resetMetrics();
        Optional<Product> result = strategy.get(1L);
        assertTrue(result.isPresent());
        assertEquals("cached", result.get().getName());
        assertEquals(1, strategy.getHits());
        assertEquals(0, strategy.getMisses());
    }

    @Test
    void shouldReturnEmptyForCacheMiss() {
        Optional<Product> result = strategy.get(999L);
        assertFalse(result.isPresent());
        assertEquals(0, strategy.getHits());
        assertEquals(1, strategy.getMisses());
    }

    @Test
    void shouldUpdateCacheOnOverwrite() {
        strategy.save(new Product(1L, "v1", 10.0, 100));
        strategy.save(new Product(1L, "v2", 20.0, 200));
        Optional<Product> result = strategy.get(1L);
        assertTrue(result.isPresent());
        assertEquals("v2", result.get().getName());
        assertEquals(20.0, result.get().getPrice());
    }

    @Test
    void shouldPersistToStoreOnWrite() {
        strategy.save(new Product(1L, "persisted", 10.0, 100));
        Optional<Product> result = store.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("persisted", result.get().getName());
    }

    @Test
    void shouldTrackHitsAndMisses() {
        strategy.save(new Product(1L, "a", 1.0, 1));
        strategy.save(new Product(2L, "b", 2.0, 2));
        strategy.resetMetrics();
        strategy.get(1L);
        strategy.get(1L);
        strategy.get(3L);
        strategy.get(2L);
        assertEquals(3, strategy.getHits());
        assertEquals(1, strategy.getMisses());
    }

    @Test
    void shouldEvictFromCache() {
        strategy.save(new Product(1L, "evict-me", 10.0, 100));
        assertNotNull(cache.get(1L));
        strategy.evict(1L);
        assertNull(cache.get(1L));
    }

    @Test
    void shouldResetMetrics() {
        strategy.save(new Product(1L, "reset", 10.0, 100));
        strategy.get(1L);
        assertTrue(strategy.getHits() > 0);
        strategy.resetMetrics();
        assertEquals(0, strategy.getHits());
        assertEquals(0, strategy.getMisses());
    }
}
