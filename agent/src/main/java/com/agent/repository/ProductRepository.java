package com.agent.repository;

import com.agent.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    @EntityGraph(attributePaths = {"category", "inventory", "images"})
    Optional<Product> findWithDetailsByProductId(Long productId);
}
