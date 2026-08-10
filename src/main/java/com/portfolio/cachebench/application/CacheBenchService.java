package com.portfolio.cachebench.application;

import com.portfolio.cachebench.domain.CacheAsideStrategy;
import com.portfolio.cachebench.domain.CacheStrategy;
import com.portfolio.cachebench.domain.Product;
import com.portfolio.cachebench.domain.ProductCache;
import com.portfolio.cachebench.domain.ProductRepository;
import com.portfolio.cachebench.domain.WriteThroughStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheBenchService {

    private final ProductCache cache;
    private final ProductRepository store;

    public CacheBenchService(ProductCache cache, ProductRepository store) {
        this.cache = cache;
        this.store = store;
    }

    public CacheAsideStrategy createCacheAsideStrategy() {
        return new CacheAsideStrategy(cache, store);
    }

    public WriteThroughStrategy createWriteThroughStrategy() {
        return new WriteThroughStrategy(cache, store);
    }

    public ProductCache getCache() {
        return cache;
    }

    public ProductRepository getStore() {
        return store;
    }

    public void loadTestData(int count) {
        store.deleteAll();
        for (int i = 1; i <= count; i++) {
            store.save(new Product((long) i, "Product-" + i, 10.0 + i, 100 + i));
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
