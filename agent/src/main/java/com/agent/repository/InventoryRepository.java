package com.agent.repository;

import com.agent.entity.Inventory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @EntityGraph(attributePaths = "product")
    Optional<Inventory> findWithProductByProductProductId(Long productId);

    @EntityGraph(attributePaths = "product")
    List<Inventory> findByProductNameContainingIgnoreCase(String name);

    Optional<Inventory> findByProductProductId(Long productId);
}
