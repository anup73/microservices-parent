package com.agent.repository;

import com.agent.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = "customer")
    List<Order> findAll();

    @EntityGraph(attributePaths = "customer")
    Optional<Order> findWithCustomerByOrderId(Long orderId);

    @EntityGraph(attributePaths = "customer")
    Optional<Order> findWithCustomerByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "customer")
    List<Order> findByCustomerCustomerId(Long customerId);

    @EntityGraph(attributePaths = "customer")
    List<Order> findDistinctByOrderItemsProductProductId(Long productId);
}
