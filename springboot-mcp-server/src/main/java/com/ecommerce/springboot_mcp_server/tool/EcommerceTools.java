package com.ecommerce.springboot_mcp_server.tool;

import com.ecommerce.springboot_mcp_server.client.EcommerceApiClient;
import com.ecommerce.springboot_mcp_server.dto.CreateProductRequest;
import com.ecommerce.springboot_mcp_server.dto.CustomerDetailsResponse;
import com.ecommerce.springboot_mcp_server.dto.InventoryResponse;
import com.ecommerce.springboot_mcp_server.dto.OrderSummaryResponse;
import com.ecommerce.springboot_mcp_server.dto.PaymentResponse;
import com.ecommerce.springboot_mcp_server.dto.ProductResponse;
import com.ecommerce.springboot_mcp_server.dto.UpdateInventoryRequest;
import com.ecommerce.springboot_mcp_server.dto.UpdateProductImagesRequest;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP tools exposing the ecommerce-api-server's order/customer/payment/product
 * operations. Every method delegates to EcommerceApiClient, which authenticates
 * outbound calls with its own OAuth2 client_credentials token (see OAuth2ClientConfig) -
 * independent of whatever token the MCP caller used to reach this server.
 */
@Component
public class EcommerceTools {

    private final EcommerceApiClient apiClient;

    public EcommerceTools(EcommerceApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = "List all orders in the ecommerce store")
    public List<OrderSummaryResponse> getAllOrders() {
        return apiClient.getAllOrders();
    }

    @Tool(description = "Get a single order by its order reference/number")
    public OrderSummaryResponse getOrderByReference(
            @ToolParam(description = "Order reference/number, e.g. ORD-1001") String orderReference) {
        return apiClient.getOrderByReference(orderReference);
    }

    @Tool(description = "List all orders that contain a given product id")
    public List<OrderSummaryResponse> getOrdersByProductId(
            @ToolParam(description = "Product id") Long productId) {
        return apiClient.getOrdersByProductId(productId);
    }

    @Tool(description = "List all orders placed by a given customer id")
    public List<OrderSummaryResponse> getOrdersByCustomerId(
            @ToolParam(description = "Customer id") Long customerId) {
        return apiClient.getOrdersByCustomerId(customerId);
    }

    @Tool(description = "List all customers in the ecommerce store")
    public List<CustomerDetailsResponse> getAllCustomers() {
        return apiClient.getAllCustomers();
    }

    @Tool(description = "Get details (including addresses) for a single customer by id")
    public CustomerDetailsResponse getCustomerDetails(
            @ToolParam(description = "Customer id") Long customerId) {
        return apiClient.getCustomerDetails(customerId);
    }

    @Tool(description = "List all payments recorded in the ecommerce store")
    public List<PaymentResponse> getAllPayments() {
        return apiClient.getAllPayments();
    }

    @Tool(description = "List all payments for a given order reference/number")
    public List<PaymentResponse> getPaymentsByOrderReference(
            @ToolParam(description = "Order reference/number, e.g. ORD-1001") String orderReference) {
        return apiClient.getPaymentsByOrderReference(orderReference);
    }

    @Tool(description = "List all payments associated with orders containing a given product id")
    public List<PaymentResponse> getPaymentsByProductId(
            @ToolParam(description = "Product id") Long productId) {
        return apiClient.getPaymentsByProductId(productId);
    }

    @Tool(description = "Get the inventory (available/reserved quantities) for a product by product id")
    public InventoryResponse getInventoryByProductId(
            @ToolParam(description = "Product id") Long productId) {
        return apiClient.getInventoryByProductId(productId);
    }

    @Tool(description = "Search inventory by product name (case-insensitive partial match)")
    public List<InventoryResponse> getInventoryByProductName(
            @ToolParam(description = "Full or partial product name") String productName) {
        return apiClient.getInventoryByProductName(productName);
    }

    @Tool(description = "Create a new product in the catalog")
    public ProductResponse createProduct(
            @ToolParam(description = "New product details, including SKU, name, price, currency, "
                    + "status, quantities and images") CreateProductRequest request) {
        return apiClient.createProduct(request);
    }

    @Tool(description = "Update the available/reserved inventory quantities for a product")
    public ProductResponse updateProductInventory(
            @ToolParam(description = "Product id") Long productId,
            @ToolParam(description = "New available and reserved quantities") UpdateInventoryRequest request) {
        return apiClient.updateProductInventory(productId, request);
    }

    @Tool(description = "Replace the images associated with a product")
    public ProductResponse updateProductImages(
            @ToolParam(description = "Product id") Long productId,
            @ToolParam(description = "New list of product images") UpdateProductImagesRequest request) {
        return apiClient.updateProductImages(productId, request);
    }
}
