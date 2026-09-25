package com.frostedcorner.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private String customerId;
    private String storeId;
    private String fulfillmentType;
    private BigDecimal fulfillmentFee;
    private String fulfillmentProvider;
    private OrderCustomer customer;
    private OrderDeliveryAddress deliveryAddress;
    private String status;
    private List<OrderItem> items;
    private BigDecimal total;
    private Instant createdAt;

    public Order() {
    }

    public Order(String id, String customerId, String storeId, String fulfillmentType,
                 BigDecimal fulfillmentFee, String fulfillmentProvider, OrderCustomer customer,
                 OrderDeliveryAddress deliveryAddress, String status, List<OrderItem> items,
                 BigDecimal total, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.storeId = storeId;
        this.fulfillmentType = fulfillmentType;
        this.fulfillmentFee = fulfillmentFee;
        this.fulfillmentProvider = fulfillmentProvider;
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.items = items;
        this.total = total;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getFulfillmentType() {
        return fulfillmentType;
    }

    public void setFulfillmentType(String fulfillmentType) {
        this.fulfillmentType = fulfillmentType;
    }

    public BigDecimal getFulfillmentFee() {
        return fulfillmentFee;
    }

    public void setFulfillmentFee(BigDecimal fulfillmentFee) {
        this.fulfillmentFee = fulfillmentFee;
    }

    public String getFulfillmentProvider() {
        return fulfillmentProvider;
    }

    public void setFulfillmentProvider(String fulfillmentProvider) {
        this.fulfillmentProvider = fulfillmentProvider;
    }

    public OrderCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(OrderCustomer customer) {
        this.customer = customer;
    }

    public OrderDeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(OrderDeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}