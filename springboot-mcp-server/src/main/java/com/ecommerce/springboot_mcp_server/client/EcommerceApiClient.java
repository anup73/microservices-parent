package com.ecommerce.springboot_mcp_server.client;

import com.ecommerce.springboot_mcp_server.dto.CreateProductRequest;
import com.ecommerce.springboot_mcp_server.dto.CustomerDetailsResponse;
import com.ecommerce.springboot_mcp_server.dto.OrderSummaryResponse;
import com.ecommerce.springboot_mcp_server.dto.PaymentResponse;
import com.ecommerce.springboot_mcp_server.dto.ProductResponse;
import com.ecommerce.springboot_mcp_server.dto.UpdateInventoryRequest;
import com.ecommerce.springboot_mcp_server.dto.UpdateProductImagesRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Thin wrapper around the ecommerce-api-server's /api/ecommerce/** endpoints. Every call
 * goes through the RestClient configured with the OAuth2 client_credentials interceptor
 * (see OAuth2ClientConfig), so no token handling is needed here.
 */
@Component
public class EcommerceApiClient {

    private final RestClient restClient;

    public EcommerceApiClient(RestClient ecommerceApiRestClient) {
        this.restClient = ecommerceApiRestClient;
    }

    public List<OrderSummaryResponse> getAllOrders() {
        return restClient.get()
                .uri("/api/ecommerce/orders")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<OrderSummaryResponse>>() {});
    }

    public OrderSummaryResponse getOrderByReference(String orderReference) {
        return restClient.get()
                .uri("/api/ecommerce/orders/{orderReference}", orderReference)
                .retrieve()
                .body(OrderSummaryResponse.class);
    }

    public List<OrderSummaryResponse> getOrdersByProductId(Long productId) {
        return restClient.get()
                .uri("/api/ecommerce/products/{productId}/orders", productId)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<OrderSummaryResponse>>() {});
    }

    public List<OrderSummaryResponse> getOrdersByCustomerId(Long customerId) {
        return restClient.get()
                .uri("/api/ecommerce/customers/{customerId}/orders", customerId)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<OrderSummaryResponse>>() {});
    }

    public List<CustomerDetailsResponse> getAllCustomers() {
        return restClient.get()
                .uri("/api/ecommerce/customers")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<CustomerDetailsResponse>>() {});
    }

    public CustomerDetailsResponse getCustomerDetails(Long customerId) {
        return restClient.get()
                .uri("/api/ecommerce/customers/{customerId}", customerId)
                .retrieve()
                .body(CustomerDetailsResponse.class);
    }

    public List<PaymentResponse> getAllPayments() {
        return restClient.get()
                .uri("/api/ecommerce/payments")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<PaymentResponse>>() {});
    }

    public List<PaymentResponse> getPaymentsByOrderReference(String orderReference) {
        return restClient.get()
                .uri("/api/ecommerce/orders/{orderReference}/payments", orderReference)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<PaymentResponse>>() {});
    }

    public List<PaymentResponse> getPaymentsByProductId(Long productId) {
        return restClient.get()
                .uri("/api/ecommerce/products/{productId}/payments", productId)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<PaymentResponse>>() {});
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        return restClient.post()
                .uri("/api/ecommerce/products")
                .body(request)
                .retrieve()
                .body(ProductResponse.class);
    }

    public ProductResponse updateProductInventory(Long productId, UpdateInventoryRequest request) {
        return restClient.put()
                .uri("/api/ecommerce/products/{productId}/inventory", productId)
                .body(request)
                .retrieve()
                .body(ProductResponse.class);
    }

    public ProductResponse updateProductImages(Long productId, UpdateProductImagesRequest request) {
        return restClient.put()
                .uri("/api/ecommerce/products/{productId}/images", productId)
                .body(request)
                .retrieve()
                .body(ProductResponse.class);
    }
}
