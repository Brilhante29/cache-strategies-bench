package com.portfolio.cachebench.domain;

import com.portfolio.cachebench.application.InMemoryCache;
import com.portfolio.cachebench.application.InMemoryProductStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CacheAsideStrategyTest {

    private InMemoryCache cache;
    private InMemoryProductStore store;
    private CacheAsideStrategy strategy;

    @BeforeEach
    void setUp() {
        cache = new InMemoryCache();
        store = new InMemoryProductStore();
        strategy = new CacheAsideStrategy(cache, store);
    }

    @Test
    void shouldReturnProductFromDbOnCacheMiss() {
        Product saved = store.save(new Product(null, "test", 10.0, 100));
        Optional<Product> result = strategy.get(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("test", result.get().getName());
    }

    @Test
    void shouldPopulateCacheAfterDbRead() {
        Product saved = store.save(new Product(null, "cached", 10.0, 100));
        strategy.get(saved.getId());
        assertNotNull(cache.get(saved.getId()));
    }

    @Test
    void shouldReturnFromCacheOnHit() {
        Product saved = store.save(new Product(null, "hot", 10.0, 100));
        strategy.get(saved.getId());
        strategy.resetMetrics();
        strategy.get(saved.getId());
        assertEquals(1, strategy.getHits());
        assertEquals(0, strategy.getMisses());
    }

    @Test
    void shouldEvictCacheOnWrite() {
        Product saved = store.save(new Product(null, "evict-me", 10.0, 100));
        strategy.get(saved.getId());
        assertNotNull(cache.get(saved.getId()));
        strategy.save(new Product(saved.getId(), "updated", 20.0, 200));
        assertNull(cache.get(saved.getId()));
    }

    @Test
    void shouldReturnUpdatedDataAfterWriteThenRead() {
        Product saved = store.save(new Product(null, "original", 10.0, 100));
        strategy.get(saved.getId());
        strategy.save(new Product(saved.getId(), "modified", 25.0, 250));
        Optional<Product> result = strategy.get(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("modified", result.get().getName());
    }

    @Test
    void shouldReturnEmptyForNonexistentProduct() {
        Optional<Product> result = strategy.get(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldTrackHitsAndMisses() {
        Product p1 = store.save(new Product(null, "a", 1.0, 1));
        Product p2 = store.save(new Product(null, "b", 2.0, 2));
        strategy.get(p1.getId());
        strategy.get(p1.getId());
        strategy.get(999L);
        strategy.get(p2.getId());
        assertEquals(1, strategy.getHits());
        assertEquals(3, strategy.getMisses());
    }

    @Test
    void shouldResetMetrics() {
        Product saved = store.save(new Product(null, "reset", 10.0, 100));
        strategy.get(saved.getId());
        assertTrue(strategy.getHits() > 0 || strategy.getMisses() > 0);
        strategy.resetMetrics();
        assertEquals(0, strategy.getHits());
        assertEquals(0, strategy.getMisses());
    }
}
