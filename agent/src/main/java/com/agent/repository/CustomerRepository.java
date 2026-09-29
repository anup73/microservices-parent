package com.agent.repository;

import com.agent.entity.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Override
    @EntityGraph(attributePaths = {"addresses"})
    List<Customer> findAll();

    @EntityGraph(attributePaths = {"addresses"})
    Optional<Customer> findWithAddressesByCustomerId(Long customerId);
}
