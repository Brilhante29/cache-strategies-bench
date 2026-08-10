package com.portfolio.cachebench.adapters.config;

import com.portfolio.cachebench.domain.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonConfigurationTest {

    @Test
    void suppliesTheMapperRequiredByRedisAndBenchmarkAdapters() throws Exception {
        var mapper = new JacksonConfiguration().objectMapper();
        var json = mapper.writeValueAsString(new Product(7L, "wired", 10.0, 3));

        assertEquals("wired", mapper.readTree(json).get("name").asText());
    }
}
