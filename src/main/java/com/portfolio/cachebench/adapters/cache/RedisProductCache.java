package com.portfolio.cachebench.adapters.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.cachebench.domain.Product;
import com.portfolio.cachebench.domain.ProductCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisProductCache implements ProductCache {

    private static final String PREFIX = "cache-bench:product:";

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final Duration ttl;

    public RedisProductCache(
            StringRedisTemplate redis,
            ObjectMapper mapper,
            @Value("${benchmark.cache-ttl:60s}") Duration ttl) {
        this.redis = redis;
        this.mapper = mapper;
        this.ttl = ttl;
    }

    @Override
    public Product get(Long key) {
        String value = redis.opsForValue().get(PREFIX + key);
        if (value == null) return null;
        try {
            return mapper.readValue(value, Product.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("invalid cached product", exception);
        }
    }

    @Override
    public void put(Long key, Product value) {
        try {
            redis.opsForValue().set(PREFIX + key, mapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("cannot serialize cached product", exception);
        }
    }

    @Override
    public void evict(Long key) {
        redis.delete(PREFIX + key);
    }

    @Override
    public void clear() {
        var keys = redis.keys(PREFIX + "*");
        if (keys != null && !keys.isEmpty()) redis.delete(keys);
    }

    @Override
    public int size() {
        var keys = redis.keys(PREFIX + "*");
        return keys == null ? 0 : keys.size();
    }
}
