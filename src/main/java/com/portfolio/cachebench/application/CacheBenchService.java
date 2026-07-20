package com.portfolio.cachebench.application;

import com.portfolio.cachebench.domain.CacheAsideStrategy;
import com.portfolio.cachebench.domain.CacheStrategy;
import com.portfolio.cachebench.domain.Product;
import com.portfolio.cachebench.domain.WriteThroughStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheBenchService {

    private final InMemoryCache cache;
    private final InMemoryProductStore store;

    public CacheBenchService(InMemoryCache cache, InMemoryProductStore store) {
        this.cache = cache;
        this.store = store;
    }

    public CacheAsideStrategy createCacheAsideStrategy() {
        return new CacheAsideStrategy(cache, store);
    }

    public WriteThroughStrategy createWriteThroughStrategy() {
        return new WriteThroughStrategy(cache, store);
    }

    public InMemoryCache getCache() {
        return cache;
    }

    public InMemoryProductStore getStore() {
        return store;
    }

    public void loadTestData(int count) {
        for (int i = 1; i <= count; i++) {
            store.save(new Product(null, "Product-" + i, 10.0 + i, 100 + i));
        }
    }

    public List<Product> getAllProducts() {
        List<Product> all = new ArrayList<>();
        for (long i = 1; i <= store.count(); i++) {
            store.findById(i).ifPresent(all::add);
        }
        return all;
    }

    public void clearCache() {
        cache.clear();
    }
}
