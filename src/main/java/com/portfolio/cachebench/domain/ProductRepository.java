package com.portfolio.cachebench.domain;

import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findById(Long id);

    Product save(Product product);

    void deleteById(Long id);
}
