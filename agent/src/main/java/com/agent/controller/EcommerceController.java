package com.agent.controller;

import com.agent.dto.CreateProductRequest;
import com.agent.dto.CustomerDetailsResponse;
import com.agent.dto.OrderSummaryResponse;
import com.agent.dto.PaymentResponse;
import com.agent.dto.ProductResponse;
import com.agent.dto.UpdateInventoryRequest;
import com.agent.dto.UpdateProductImagesRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.agent.service.EcommerceQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ecommerce")
public class EcommerceController {

    private final EcommerceQueryService ecommerceQueryService;

    public EcommerceController(EcommerceQueryService ecommerceQueryService) {
        this.ecommerceQueryService = ecommerceQueryService;
    }

    @GetMapping("/orders")
    public List<OrderSummaryResponse> getAllOrders() {
        return ecommerceQueryService.getAllOrders();
    }

    @GetMapping("/customers")
    public List<CustomerDetailsResponse> getAllCustomers() {
        return ecommerceQueryService.getAllCustomers();
    }

    @GetMapping("/orders/{orderReference}")
    public OrderSummaryResponse getOrderById(@PathVariable String orderReference) {
        return ecommerceQueryService.getOrderByReference(orderReference);
    }

    @GetMapping("/products/{productId}/orders")
    public List<OrderSummaryResponse> getOrdersByProductId(@PathVariable Long productId) {
        return ecommerceQueryService.getOrdersByProductId(productId);
    }

    @GetMapping("/customers/{customerId}")
    public CustomerDetailsResponse getCustomerDetails(@PathVariable Long customerId) {
        return ecommerceQueryService.getCustomerDetails(customerId);
    }

    @GetMapping("/customers/{customerId}/orders")
    public List<OrderSummaryResponse> getOrdersByCustomerId(@PathVariable Long customerId) {
        return ecommerceQueryService.getOrdersByCustomerId(customerId);
    }

    @GetMapping("/orders/{orderReference}/payments")
    public List<PaymentResponse> getPaymentsByOrderId(@PathVariable String orderReference) {
        return ecommerceQueryService.getPaymentsByOrderReference(orderReference);
    }

    @GetMapping("/payments")
    public List<PaymentResponse> getAllPayments() {
        return ecommerceQueryService.getAllPayments();
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ecommerceQueryService.createProduct(request);
    }

    @PutMapping("/products/{productId}/inventory")
    public ProductResponse updateProductInventory(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateInventoryRequest request
    ) {
        return ecommerceQueryService.updateProductInventory(productId, request);
    }

    @PutMapping("/products/{productId}/images")
    public ProductResponse updateProductImages(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductImagesRequest request
    ) {
        return ecommerceQueryService.updateProductImages(productId, request);
    }

    @GetMapping("/products/{productId}/payments")
    public List<PaymentResponse> getPaymentsByProductId(@PathVariable Long productId) {
        return ecommerceQueryService.getPaymentsByProductId(productId);
    }
}
