package com.portfolio.cachebench.domain;

public interface ProductCache {

    Product get(Long key);

    void put(Long key, Product value);

    void evict(Long key);

    void clear();

    int size();
}
