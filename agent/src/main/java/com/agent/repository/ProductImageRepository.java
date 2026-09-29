package com.agent.repository;

import com.agent.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductProductIdOrderByDisplayOrderAsc(Long productId);
}
