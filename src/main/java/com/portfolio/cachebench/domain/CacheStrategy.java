package com.portfolio.cachebench.domain;

import java.util.Optional;

public interface CacheStrategy {

    Optional<Product> get(Long id);

    Product save(Product product);

    void evict(Long id);

    String getName();

    long getHits();

    long getMisses();

    void resetMetrics();
}
