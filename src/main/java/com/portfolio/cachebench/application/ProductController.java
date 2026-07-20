package com.portfolio.cachebench.application;

import com.portfolio.cachebench.domain.CacheStrategy;
import com.portfolio.cachebench.domain.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CacheBenchService benchService;
    private CacheStrategy currentStrategy;

    public ProductController(CacheBenchService benchService) {
        this.benchService = benchService;
        this.currentStrategy = benchService.createCacheAsideStrategy();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Optional<Product> product = currentStrategy.get(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return currentStrategy.save(product);
    }

    @PostMapping("/strategy/{name}")
    public void switchStrategy(@PathVariable String name) {
        this.currentStrategy = switch (name) {
            case "cache-aside" -> benchService.createCacheAsideStrategy();
            case "write-through" -> benchService.createWriteThroughStrategy();
            default -> throw new IllegalArgumentException("Unknown strategy: " + name);
        };
    }
}
