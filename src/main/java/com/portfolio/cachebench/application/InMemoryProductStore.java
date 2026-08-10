package com.portfolio.cachebench.application;

import com.portfolio.cachebench.domain.Product;
import com.portfolio.cachebench.domain.ProductRepository;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryProductStore implements ProductRepository {

    static final long MIN_LATENCY_MS = 1;
    static final long MAX_LATENCY_MS = 5;

    private final ConcurrentHashMap<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Random random = new Random(42);

    @Override
    public Optional<Product> findById(Long id) {
        simulateLatency();
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Product save(Product product) {
        simulateLatency();
        if (product.getId() == null) {
            product.setId(nextId.getAndIncrement());
        }
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public void deleteById(Long id) {
        simulateLatency();
        store.remove(id);
    }

    @Override
    public int count() {
        return store.size();
    }

    @Override
    public void deleteAll() {
        store.clear();
        nextId.set(1);
    }

    void simulateLatency() {
        long delay = MIN_LATENCY_MS + random.nextLong(MAX_LATENCY_MS - MIN_LATENCY_MS + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
