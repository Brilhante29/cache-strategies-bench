package com.portfolio.cachebench.domain;

import com.portfolio.cachebench.application.InMemoryCache;

import java.util.Optional;

public class WriteThroughStrategy implements CacheStrategy {

    private final InMemoryCache cache;
    private final ProductRepository repository;
    private long hits;
    private long misses;

    public WriteThroughStrategy(InMemoryCache cache, ProductRepository repository) {
        this.cache = cache;
        this.repository = repository;
    }

    @Override
    public Optional<Product> get(Long id) {
        Product cached = cache.get(id);
        if (cached != null) {
            hits++;
            return Optional.of(cached);
        }
        misses++;
        return Optional.empty();
    }

    @Override
    public Product save(Product product) {
        Product saved = repository.save(product);
        cache.put(product.getId(), saved);
        return saved;
    }

    @Override
    public void evict(Long id) {
        cache.evict(id);
        repository.deleteById(id);
    }

    @Override
    public String getName() {
        return "write-through";
    }

    @Override
    public long getHits() {
        return hits;
    }

    @Override
    public long getMisses() {
        return misses;
    }

    @Override
    public void resetMetrics() {
        hits = 0;
        misses = 0;
    }
}
