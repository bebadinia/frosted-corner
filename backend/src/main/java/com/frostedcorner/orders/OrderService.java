package com.frostedcorner.orders;

import com.frostedcorner.catalog.Product;
import com.frostedcorner.catalog.ProductRepository;
import com.frostedcorner.inventory.InventoryDeduction;
import com.frostedcorner.inventory.InventoryService;
import com.frostedcorner.locations.FranchiseLocation;
import com.frostedcorner.locations.FranchiseLocationDistanceResponse;
import com.frostedcorner.locations.FranchiseLocationRepository;
import com.frostedcorner.locations.FranchiseLocationService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final String CONFIRMED_STATUS = "CONFIRMED";
    private static final double LOCAL_DELIVERY_MAX_DISTANCE_MILES = 25.0;
    private static final BigDecimal LOCAL_DELIVERY_FEE = new BigDecimal("2.99");
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("4.99");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("25.00");
    private static final BigDecimal FREE_FULFILLMENT_FEE = new BigDecimal("0.00");
    private static final BigDecimal TAKEOUT_FEE = BigDecimal.ZERO;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final FranchiseLocationService franchiseLocationService;
    private final FranchiseLocationRepository franchiseLocationRepository;
    private final LocalDeliveryProviderAssigner localDeliveryProviderAssigner;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        InventoryService inventoryService,
                        FranchiseLocationService franchiseLocationService,
                        FranchiseLocationRepository franchiseLocationRepository,
                        LocalDeliveryProviderAssigner localDeliveryProviderAssigner) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.franchiseLocationService = franchiseLocationService;
        this.franchiseLocationRepository = franchiseLocationRepository;
        this.localDeliveryProviderAssigner = localDeliveryProviderAssigner;
    }

    public OrderQuoteResponse quoteOrder(CreateOrderRequest request) {
        validateRequest(request);

        ResolvedFulfillment fulfillment = resolveFulfillment(request);
        BigDecimal subtotal = calculateSubtotal(request.items());
        BigDecimal fulfillmentFee = promotionalFulfillmentFee(
                request.fulfillmentOption(), subtotal, fulfillment.fulfillmentFee());
        boolean promotionApplied = fulfillment.fulfillmentFee().compareTo(fulfillmentFee) > 0;

        return new OrderQuoteResponse(
                subtotal,
                fulfillmentFee,
                subtotal.add(fulfillmentFee),
                fulfillment.fulfillmentType().name(),
                promotionApplied);
    }

    public Order createOrder(CreateOrderRequest request) {
        validateRequest(request);

        ResolvedFulfillment fulfillment = resolveFulfillment(request);
        Map<String, Integer> requestedQuantities = new LinkedHashMap<>();
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderItemRequest requestedItem : request.items()) {
            Product product = productRepository.findById(requestedItem.productId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ProductNotFoundException(requestedItem.productId()));
            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(requestedItem.quantity()));

            requestedQuantities.put(product.getId(), requestedItem.quantity());
            orderItems.add(new OrderItem(product.getId(), product.getName(), product.getPrice(),
                    requestedItem.quantity(), lineTotal));
        subtotal = subtotal.add(lineTotal);
        }

        List<InventoryDeduction> deductions = inventoryService.validateAvailability(
        fulfillment.storeId(), requestedQuantities);
        BigDecimal fulfillmentFee = promotionalFulfillmentFee(
                request.fulfillmentOption(), subtotal, fulfillment.fulfillmentFee());
    Order order = new Order(null, request.customerId(), fulfillment.storeId(),
        fulfillment.fulfillmentType().name(), fulfillmentFee,
        fulfillment.fulfillmentProvider(), fulfillment.customer(),
        fulfillment.deliveryAddress(), CONFIRMED_STATUS, orderItems,
        subtotal.add(fulfillmentFee), Instant.now());
        Order savedOrder = orderRepository.save(order);
        inventoryService.applyDeductions(deductions);
        return savedOrder;
    }

    private BigDecimal calculateSubtotal(List<CreateOrderItemRequest> requestedItems) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderItemRequest requestedItem : requestedItems) {
            Product product = productRepository.findById(requestedItem.productId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ProductNotFoundException(requestedItem.productId()));
            subtotal = subtotal.add(product.getPrice()
                    .multiply(BigDecimal.valueOf(requestedItem.quantity())));
        }

        return subtotal;
    }

    private void validateRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new InvalidOrderException("order request is required");
        }
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new InvalidOrderException("customerId is required");
        }
        if (request.fulfillmentOption() == null) {
            throw new InvalidOrderException("fulfillmentOption is required");
        }
        if (request.customer() == null) {
            throw new InvalidOrderException("customer is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidOrderException("items are required");
        }

        validateContactFields(request.customer());
        if (request.fulfillmentOption() == CheckoutFulfillmentOption.DELIVERY) {
            validateDeliveryAddressFields(request.customer());
        }
        if (request.fulfillmentOption() == CheckoutFulfillmentOption.TAKEOUT
                && (request.storeId() == null || request.storeId().isBlank())) {
            throw new InvalidOrderException("storeId is required for takeout");
        }

        Set<String> productIds = new HashSet<>();
        for (CreateOrderItemRequest item : request.items()) {
            if (item == null || item.productId() == null || item.productId().isBlank()) {
                throw new InvalidOrderException("productId is required");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new InvalidOrderException("quantity must be greater than zero");
            }
            if (!productIds.add(item.productId())) {
                throw new InvalidOrderException("duplicate productId: " + item.productId());
            }
        }
    }

    private void validateContactFields(CreateOrderCustomerRequest customer) {
        requireField(customer.name(), "customer.name is required");
        requireField(customer.email(), "customer.email is required");
        requireField(customer.phone(), "customer.phone is required");
    }

    private void validateDeliveryAddressFields(CreateOrderCustomerRequest customer) {
        requireField(customer.street(), "customer.street is required for delivery");
        requireField(customer.city(), "customer.city is required for delivery");
        requireField(customer.state(), "customer.state is required for delivery");
        requireField(customer.zipCode(), "customer.zipCode is required for delivery");
    }

    private void requireField(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderException(message);
        }
    }

    private ResolvedFulfillment resolveFulfillment(CreateOrderRequest request) {
        OrderCustomer customer = new OrderCustomer(request.customer().name(),
                request.customer().email(), request.customer().phone());

        if (request.fulfillmentOption() == CheckoutFulfillmentOption.TAKEOUT) {
            FranchiseLocation store = franchiseLocationRepository.findById(request.storeId())
                    .orElseThrow(() -> new StoreNotFoundException(request.storeId()));
            return new ResolvedFulfillment(FulfillmentType.TAKEOUT, store.getId(), TAKEOUT_FEE,
                    null, customer, null);
        }

        FranchiseLocationDistanceResponse nearestStore = franchiseLocationService
                .rankStoresByDistance(formatDemoAddress(request.customer()))
                .nearestStore();
        OrderDeliveryAddress deliveryAddress = new OrderDeliveryAddress(request.customer().street(),
                request.customer().city(), request.customer().state(), request.customer().zipCode());

        if (nearestStore.distanceMiles() <= LOCAL_DELIVERY_MAX_DISTANCE_MILES) {
            return new ResolvedFulfillment(FulfillmentType.LOCAL_DELIVERY, nearestStore.id(),
                    LOCAL_DELIVERY_FEE, localDeliveryProviderAssigner.assignProvider(),
                    customer, deliveryAddress);
        }

        return new ResolvedFulfillment(FulfillmentType.SHIPPING, nearestStore.id(), SHIPPING_FEE,
                null, customer, deliveryAddress);
    }

    private BigDecimal promotionalFulfillmentFee(
            CheckoutFulfillmentOption fulfillmentOption,
            BigDecimal subtotal,
            BigDecimal standardFee) {
        if (fulfillmentOption == CheckoutFulfillmentOption.DELIVERY
                && subtotal.compareTo(FREE_SHIPPING_THRESHOLD) > 0) {
            return FREE_FULFILLMENT_FEE;
        }
        return standardFee;
    }

    private String formatDemoAddress(CreateOrderCustomerRequest customer) {
        return "%s, %s, %s %s".formatted(customer.street().trim(), customer.city().trim(),
                customer.state().trim(), customer.zipCode().trim());
    }

    private record ResolvedFulfillment(FulfillmentType fulfillmentType,
                                       String storeId,
                                       BigDecimal fulfillmentFee,
                                       String fulfillmentProvider,
                                       OrderCustomer customer,
                                       OrderDeliveryAddress deliveryAddress) {
    }
}