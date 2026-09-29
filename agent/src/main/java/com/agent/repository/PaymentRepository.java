package com.agent.repository;

import com.agent.entity.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Override
    @EntityGraph(attributePaths = {"order", "order.customer"})
    List<Payment> findAll();

    @EntityGraph(attributePaths = {"order", "order.customer"})
    List<Payment> findByOrderOrderId(Long orderId);

    @EntityGraph(attributePaths = {"order", "order.customer"})
    List<Payment> findDistinctByOrderOrderItemsProductProductId(Long productId);
}
