package com.portfolio.cachebench.adapters.persistence;

import com.portfolio.cachebench.domain.Product;
import com.portfolio.cachebench.domain.ProductRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcProductRepository implements ProductRepository {

    private final JdbcClient jdbc;

    public JdbcProductRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jdbc.sql("SELECT id, name, price, stock FROM products WHERE id = :id")
                .param("id", id)
                .query((rs, row) -> new Product(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")))
                .optional();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            Long id = jdbc.sql("INSERT INTO products(name, price, stock) VALUES (:name, :price, :stock) RETURNING id")
                    .param("name", product.getName())
                    .param("price", product.getPrice())
                    .param("stock", product.getStock())
                    .query(Long.class)
                    .single();
            product.setId(id);
            return product;
        }
        jdbc.sql("""
                INSERT INTO products(id, name, price, stock)
                VALUES (:id, :name, :price, :stock)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name, price = EXCLUDED.price, stock = EXCLUDED.stock
                """)
                .param("id", product.getId())
                .param("name", product.getName())
                .param("price", product.getPrice())
                .param("stock", product.getStock())
                .update();
        return product;
    }

    @Override
    public void deleteById(Long id) {
        jdbc.sql("DELETE FROM products WHERE id = :id").param("id", id).update();
    }

    @Override
    public int count() {
        return jdbc.sql("SELECT count(*) FROM products").query(Integer.class).single();
    }

    @Override
    public void deleteAll() {
        jdbc.sql("TRUNCATE TABLE products RESTART IDENTITY").update();
    }
}
