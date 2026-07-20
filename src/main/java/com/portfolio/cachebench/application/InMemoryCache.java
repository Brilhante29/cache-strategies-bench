package com.portfolio.cachebench.application;

import com.portfolio.cachebench.domain.Product;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCache {

    static final long DEFAULT_TTL_MILLIS = 60_000;

    private final ConcurrentHashMap<Long, CacheEntry> map = new ConcurrentHashMap<>();

    public Product get(Long key) {
        CacheEntry entry = map.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() > entry.expiry) {
            map.remove(key);
            return null;
        }
        return entry.value;
    }

    public void put(Long key, Product value) {
        map.put(key, new CacheEntry(value, System.currentTimeMillis() + DEFAULT_TTL_MILLIS));
    }

    public void evict(Long key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }

    static class CacheEntry {
        final Product value;
        final long expiry;

        CacheEntry(Product value, long expiry) {
            this.value = value;
            this.expiry = expiry;
        }
    }
}
