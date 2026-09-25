package com.frostedcorner.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.frostedcorner.catalog.Product;
import com.frostedcorner.catalog.ProductRepository;
import com.frostedcorner.inventory.InsufficientInventoryException;
import com.frostedcorner.inventory.Inventory;
import com.frostedcorner.inventory.InventoryDeduction;
import com.frostedcorner.inventory.InventoryService;
import com.frostedcorner.locations.DemoCustomerCoordinates;
import com.frostedcorner.locations.FranchiseLocation;
import com.frostedcorner.locations.FranchiseLocationDistanceResponse;
import com.frostedcorner.locations.FranchiseLocationRepository;
import com.frostedcorner.locations.FranchiseLocationService;
import com.frostedcorner.locations.LocationDistanceRankingResponse;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private FranchiseLocationService franchiseLocationService;

    @Mock
    private FranchiseLocationRepository franchiseLocationRepository;

    @Mock
    private LocalDeliveryProviderAssigner localDeliveryProviderAssigner;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository, inventoryService,
                franchiseLocationService, franchiseLocationRepository, localDeliveryProviderAssigner);
    }

    @Test
    void createsLocalDeliveryOrderUsingBackendPricesAndDecrementsInventory() {
        Product cake = product("P001", "Chocolate Cake", "32.99", true);
        Product cupcake = product("P005", "Chocolate Cupcake", "4.49", true);
        when(productRepository.findById("P001")).thenReturn(Optional.of(cake));
        when(productRepository.findById("P005")).thenReturn(Optional.of(cupcake));
        when(localDeliveryProviderAssigner.assignProvider()).thenReturn("DoorDash");
        when(franchiseLocationService.rankStoresByDistance("2490 Burnside Street, Portland, OR 97205"))
                .thenReturn(rankedStores("97205", nearestStore("store24", 0.0)));
        Map<String, Integer> quantities = new LinkedHashMap<>();
        quantities.put("P001", 2);
        quantities.put("P005", 3);
        List<InventoryDeduction> deductions = List.of(
                new InventoryDeduction(new Inventory("inv1", "store24", "P001", 20, 10), 2),
                new InventoryDeduction(new Inventory("inv2", "store24", "P005", 30, 10), 3));
        when(inventoryService.validateAvailability("store24", quantities)).thenReturn(deductions);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId("o100");
            return order;
        });

        Order result = orderService.createOrder(deliveryRequest(List.of(
                new CreateOrderItemRequest("P001", 2),
                new CreateOrderItemRequest("P005", 3))));

        assertEquals("o100", result.getId());
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals("store24", result.getStoreId());
        assertEquals("LOCAL_DELIVERY", result.getFulfillmentType());
        assertEquals(new BigDecimal("0.00"), result.getFulfillmentFee());
        assertEquals("DoorDash", result.getFulfillmentProvider());
        assertEquals(new BigDecimal("79.45"), result.getTotal());
        assertEquals(new BigDecimal("65.98"), result.getItems().get(0).getLineTotal());
        assertEquals(new BigDecimal("13.47"), result.getItems().get(1).getLineTotal());
        assertEquals(cake.getPrice(), result.getItems().get(0).getUnitPrice());
        assertEquals("Alex Carter", result.getCustomer().getName());
        assertEquals("2490 Burnside Street", result.getDeliveryAddress().getStreet());
        assertNotNull(result.getCreatedAt());
        verify(inventoryService).applyDeductions(deductions);
    }

    @Test
    void createsShippingOrderWhenNearestStoreIsBeyondLocalDeliveryRadius() {
        Product cake = product("P001", "Chocolate Cake", "32.99", true);
        when(productRepository.findById("P001")).thenReturn(Optional.of(cake));
        when(franchiseLocationService.rankStoresByDistance("2490 Burnside Street, Portland, OR 97205"))
                .thenReturn(rankedStores("97205", nearestStore("store17", 25.01)));
        List<InventoryDeduction> deductions = List.of(
                new InventoryDeduction(new Inventory("inv1", "store17", "P001", 20, 10), 1));
        when(inventoryService.validateAvailability("store17", Map.of("P001", 1))).thenReturn(deductions);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(deliveryRequest(List.of(
                new CreateOrderItemRequest("P001", 1))));

        assertEquals("SHIPPING", result.getFulfillmentType());
        assertEquals("store17", result.getStoreId());
        assertEquals(new BigDecimal("0.00"), result.getFulfillmentFee());
        assertNull(result.getFulfillmentProvider());
        assertEquals(new BigDecimal("32.99"), result.getTotal());
        verify(inventoryService).applyDeductions(deductions);
    }

    @Test
    void keepsShippingFeeWhenSubtotalIsExactlyTwentyFiveDollars() {
        Product product = product("P001", "Dessert Box", "25.00", true);
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));
        when(franchiseLocationService.rankStoresByDistance("2490 Burnside Street, Portland, OR 97205"))
                .thenReturn(rankedStores("97205", nearestStore("store17", 25.01)));
        when(inventoryService.validateAvailability("store17", Map.of("P001", 1)))
                .thenReturn(List.of(new InventoryDeduction(
                        new Inventory("inv1", "store17", "P001", 20, 10), 1)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(deliveryRequest(List.of(
                new CreateOrderItemRequest("P001", 1))));

        assertEquals(new BigDecimal("4.99"), result.getFulfillmentFee());
        assertEquals(new BigDecimal("29.99"), result.getTotal());
    }

    @Test
    void createsTakeoutOrderWithoutRequiringDeliveryAddress() {
        Product cupcake = product("P005", "Chocolate Cupcake", "4.49", true);
        when(productRepository.findById("P005")).thenReturn(Optional.of(cupcake));
        when(franchiseLocationRepository.findById("store1")).thenReturn(Optional.of(
                new FranchiseLocation("store1", "Frosted Corner - New York", "101 Broadway",
                        "New York", "NY", "10001", "Morgan Lee", 40.7128, -74.0060)));
        List<InventoryDeduction> deductions = List.of(
                new InventoryDeduction(new Inventory("inv2", "store1", "P005", 30, 10), 2));
        when(inventoryService.validateAvailability("store1", Map.of("P005", 2))).thenReturn(deductions);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(takeoutRequest("store1", List.of(
                new CreateOrderItemRequest("P005", 2))));

        assertEquals("TAKEOUT", result.getFulfillmentType());
        assertEquals(new BigDecimal("0.00"), result.getFulfillmentFee());
        assertNull(result.getFulfillmentProvider());
        assertNull(result.getDeliveryAddress());
        assertEquals(new BigDecimal("8.98"), result.getTotal());
        verify(inventoryService).applyDeductions(deductions);
    }

    @Test
    void rejectsUnknownProductWithoutPersistingOrDecrementing() {
        when(franchiseLocationRepository.findById("store1")).thenReturn(Optional.of(
                new FranchiseLocation("store1", "Frosted Corner - New York", "101 Broadway",
                        "New York", "NY", "10001", "Morgan Lee", 40.7128, -74.0060)));
        when(productRepository.findById("P999")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> orderService.createOrder(takeoutRequest("store1", List.of(
                        new CreateOrderItemRequest("P999", 1)))));

        verifyNoInteractions(orderRepository, inventoryService, franchiseLocationService);
    }

    @Test
    void rejectsInactiveProduct() {
        when(franchiseLocationRepository.findById("store1")).thenReturn(Optional.of(
                new FranchiseLocation("store1", "Frosted Corner - New York", "101 Broadway",
                        "New York", "NY", "10001", "Morgan Lee", 40.7128, -74.0060)));
        when(productRepository.findById("P020"))
                .thenReturn(Optional.of(product("P020", "Seasonal Cupcake", "4.99", false)));

        assertThrows(ProductNotFoundException.class,
                () -> orderService.createOrder(takeoutRequest("store1", List.of(
                        new CreateOrderItemRequest("P020", 1)))));

        verifyNoInteractions(orderRepository, inventoryService, franchiseLocationService);
    }

    @Test
    void rejectsInvalidQuantityBeforeLookingUpProducts() {
        CreateOrderRequest request = takeoutRequest("store1", List.of(
                new CreateOrderItemRequest("P001", 0)));

        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request));

        verifyNoInteractions(productRepository, orderRepository, inventoryService,
                franchiseLocationRepository, franchiseLocationService);
    }

    @Test
    void rejectsDuplicateProductsBeforeLookingUpProducts() {
        CreateOrderRequest request = takeoutRequest("store1", List.of(
                new CreateOrderItemRequest("P001", 1),
                new CreateOrderItemRequest("P001", 2)));

        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request));

        verifyNoInteractions(productRepository, orderRepository, inventoryService,
                franchiseLocationRepository, franchiseLocationService);
    }

    @Test
    void insufficientInventoryDoesNotPersistOrDecrement() {
        Product product = product("P001", "Chocolate Cake", "32.99", true);
        when(franchiseLocationRepository.findById("store1")).thenReturn(Optional.of(
                new FranchiseLocation("store1", "Frosted Corner - New York", "101 Broadway",
                        "New York", "NY", "10001", "Morgan Lee", 40.7128, -74.0060)));
        when(productRepository.findById("P001")).thenReturn(Optional.of(product));
        Map<String, Integer> quantities = Map.of("P001", 25);
        when(inventoryService.validateAvailability("store1", quantities))
                .thenThrow(new InsufficientInventoryException("store1", "P001", 25, 20));

        assertThrows(InsufficientInventoryException.class,
                () -> orderService.createOrder(takeoutRequest("store1", List.of(
                        new CreateOrderItemRequest("P001", 25)))));

        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryService, never()).applyDeductions(any());
    }

    @Test
    void rejectsEmptyOrder() {
        CreateOrderRequest request = takeoutRequest("store1", List.of());

        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request));

        verifyNoInteractions(productRepository, orderRepository, inventoryService,
                franchiseLocationRepository, franchiseLocationService);
    }

    @Test
    void rejectsDeliveryWithoutRequiredAddressFields() {
        CreateOrderRequest request = new CreateOrderRequest("c1", null,
                CheckoutFulfillmentOption.DELIVERY,
                new CreateOrderCustomerRequest("Alex Carter", "alex@example.com", "555-0100",
                        "", "Portland", "OR", "97205"),
                List.of(new CreateOrderItemRequest("P001", 1)));

        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(request));

        verifyNoInteractions(productRepository, orderRepository, inventoryService,
                franchiseLocationRepository, franchiseLocationService);
    }

    private CreateOrderRequest deliveryRequest(List<CreateOrderItemRequest> items) {
        return new CreateOrderRequest("c1", null, CheckoutFulfillmentOption.DELIVERY,
                new CreateOrderCustomerRequest("Alex Carter", "alex@example.com", "555-0100",
                        "2490 Burnside Street", "Portland", "OR", "97205"),
                items);
    }

    private CreateOrderRequest takeoutRequest(String storeId, List<CreateOrderItemRequest> items) {
        return new CreateOrderRequest("c1", storeId, CheckoutFulfillmentOption.TAKEOUT,
                new CreateOrderCustomerRequest("Alex Carter", "alex@example.com", "555-0100",
                        null, null, null, null),
                items);
    }

    private Product product(String id, String name, String price, boolean active) {
        return new Product(id, name, "Description", new BigDecimal(price),
                "Category", "image.jpg", active);
    }

    private LocationDistanceRankingResponse rankedStores(String matchedLocation,
                                                         FranchiseLocationDistanceResponse nearestStore) {
        return new LocationDistanceRankingResponse(
                new DemoCustomerCoordinates(matchedLocation, 45.5152, -122.6784),
                nearestStore,
                List.of(nearestStore));
    }

    private FranchiseLocationDistanceResponse nearestStore(String storeId, double distanceMiles) {
        return new FranchiseLocationDistanceResponse(storeId, "Frosted Corner", "Street",
                "City", "ST", "00000", "Manager", 0.0, 0.0, distanceMiles);
    }
}