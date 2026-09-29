package com.agent.service;

import com.agent.dto.CreateProductRequest;
import com.agent.dto.CreateProductImageRequest;
import com.agent.dto.CustomerSummaryResponse;
import com.agent.dto.CustomerAddressResponse;
import com.agent.dto.CustomerDetailsResponse;
import com.agent.dto.OrderSummaryResponse;
import com.agent.dto.PaymentResponse;
import com.agent.dto.ProductImageResponse;
import com.agent.dto.ProductResponse;
import com.agent.dto.UpdateInventoryRequest;
import com.agent.dto.UpdateProductImagesRequest;
import com.agent.entity.Category;
import com.agent.entity.Customer;
import com.agent.entity.CustomerAddress;
import com.agent.entity.Inventory;
import com.agent.entity.Order;
import com.agent.entity.Payment;
import com.agent.entity.Product;
import com.agent.entity.ProductImage;
import com.agent.repository.CategoryRepository;
import com.agent.repository.CustomerRepository;
import com.agent.repository.InventoryRepository;
import com.agent.repository.OrderRepository;
import com.agent.repository.PaymentRepository;
import com.agent.repository.ProductImageRepository;
import com.agent.repository.ProductRepository;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EcommerceQueryService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final InventoryRepository inventoryRepository;

    public EcommerceQueryService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository productImageRepository,
            InventoryRepository inventoryRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<OrderSummaryResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toOrderSummary)
                .toList();
    }

    public List<CustomerDetailsResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toCustomerDetails)
                .toList();
    }

    public OrderSummaryResponse getOrderByReference(String orderReference) {
        return findOrderByReference(orderReference)
                .map(this::toOrderSummary)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for reference " + orderReference));
    }

    public List<OrderSummaryResponse> getOrdersByProductId(Long productId) {
        return orderRepository.findDistinctByOrderItemsProductProductId(productId).stream()
                .map(this::toOrderSummary)
                .toList();
    }

    public CustomerDetailsResponse getCustomerDetails(Long customerId) {
        return customerRepository.findWithAddressesByCustomerId(customerId)
                .map(this::toCustomerDetails)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for id " + customerId));
    }

    public List<OrderSummaryResponse> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerCustomerId(customerId).stream()
                .map(this::toOrderSummary)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByOrderReference(String orderReference) {
        Order order = findOrderByReference(orderReference)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for reference " + orderReference));

        return paymentRepository.findByOrderOrderId(order.getOrderId()).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByProductId(Long productId) {
        return paymentRepository.findDistinctByOrderOrderItemsProductProductId(productId).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new ValidationException("Product already exists for SKU " + request.sku());
        }
        if (request.quantityAvailable() < 0) {
            throw new ValidationException("Available quantity must be zero or greater");
        }

        int quantityReserved = request.quantityReserved() == null ? 0 : request.quantityReserved();
        if (quantityReserved < 0) {
            throw new ValidationException("Reserved quantity must be zero or greater");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for id " + request.categoryId()));

        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setPrice(request.price());
        product.setCurrency(request.currency());
        product.setStatus(request.status());
        Product savedProduct = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(savedProduct);
        inventory.setQuantityAvailable(request.quantityAvailable());
        inventory.setQuantityReserved(quantityReserved);
        Inventory savedInventory = inventoryRepository.save(inventory);
        savedProduct.setInventory(savedInventory);

        List<ProductImage> savedImages = persistImages(savedProduct, request);
        return toProductResponse(savedProduct, savedImages, savedInventory);
    }

    @Transactional
    public ProductResponse updateProductInventory(Long productId, UpdateInventoryRequest request) {
        if (request.quantityAvailable() < 0) {
            throw new ValidationException("Available quantity must be zero or greater");
        }
        if (request.quantityReserved() < 0) {
            throw new ValidationException("Reserved quantity must be zero or greater");
        }

        Product product = productRepository.findWithDetailsByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id " + productId));

        Inventory inventory = inventoryRepository.findByProductProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id " + productId));

        inventory.setQuantityAvailable(request.quantityAvailable());
        inventory.setQuantityReserved(request.quantityReserved());
        Inventory savedInventory = inventoryRepository.save(inventory);

        List<ProductImage> images = productImageRepository.findByProductProductIdOrderByDisplayOrderAsc(productId);
        return toProductResponse(product, images, savedInventory);
    }

    @Transactional
    public ProductResponse updateProductImages(Long productId, UpdateProductImagesRequest request) {
        Product product = productRepository.findWithDetailsByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for id " + productId));

        Inventory inventory = inventoryRepository.findByProductProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id " + productId));

        List<ProductImage> existingImages = productImageRepository.findByProductProductIdOrderByDisplayOrderAsc(productId);
        productImageRepository.deleteAll(existingImages);

        List<ProductImage> savedImages = persistImages(product, request.images());
        return toProductResponse(product, savedImages, inventory);
    }

    private java.util.Optional<Order> findOrderByReference(String orderReference) {
        try {
            return orderRepository.findWithCustomerByOrderId(Long.parseLong(orderReference));
        } catch (NumberFormatException exception) {
            return orderRepository.findWithCustomerByOrderNumber(orderReference);
        }
    }

    private OrderSummaryResponse toOrderSummary(Order order) {
        return new OrderSummaryResponse(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderStatus(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getShippingAmount(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                toCustomerSummary(order.getCustomer())
        );
    }

    private CustomerSummaryResponse toCustomerSummary(Customer customer) {
        return new CustomerSummaryResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus()
        );
    }

    private CustomerDetailsResponse toCustomerDetails(Customer customer) {
        return new CustomerDetailsResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getAddresses().stream()
                        .map(this::toCustomerAddress)
                        .toList()
        );
    }

    private CustomerAddressResponse toCustomerAddress(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getAddressId(),
                address.getAddressType(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getPhone(),
                address.getDefault()
        );
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrder().getOrderId(),
                payment.getOrder().getOrderNumber(),
                payment.getOrder().getCustomer().getCustomerId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTransactionReference(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private List<ProductImage> persistImages(Product product, CreateProductRequest request) {
        return persistImages(product, request.images());
    }

    private List<ProductImage> persistImages(Product product, List<CreateProductImageRequest> imageRequests) {
        for (int index = 0; index < imageRequests.size(); index++) {
            if (imageRequests.get(index).displayOrder() != null && imageRequests.get(index).displayOrder() <= 0) {
                throw new ValidationException("Image display order must be greater than zero");
            }
        }

        boolean hasPrimaryImage = imageRequests.stream()
                .anyMatch(image -> Boolean.TRUE.equals(image.primary()));

        return imageRequests.stream()
                .map(imageRequest -> {
                    ProductImage image = new ProductImage();
                    image.setProduct(product);
                    image.setImageUrl(imageRequest.imageUrl());
                    image.setDisplayOrder(imageRequest.displayOrder() == null ? 1 : imageRequest.displayOrder());
                    image.setPrimary(hasPrimaryImage ? Boolean.TRUE.equals(imageRequest.primary()) : false);
                    return productImageRepository.save(image);
                })
                .toList();
    }

    private ProductResponse toProductResponse(Product product, List<ProductImage> images, Inventory inventory) {
        return new ProductResponse(
                product.getProductId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory() == null ? null : product.getCategory().getCategoryId(),
                product.getCategory() == null ? null : product.getCategory().getName(),
                product.getPrice(),
                product.getCurrency(),
                product.getStatus(),
                inventory.getQuantityAvailable(),
                inventory.getQuantityReserved(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                images.stream()
                        .map(image -> new ProductImageResponse(
                                image.getProductImageId(),
                                image.getImageUrl(),
                                image.getDisplayOrder(),
                                image.getPrimary()
                        ))
                        .toList()
        );
    }
}
