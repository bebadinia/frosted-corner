package com.frostedcorner.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "inventory")
@CompoundIndex(name = "store_product_unique", def = "{'storeId': 1, 'productId': 1}", unique = true)
public class Inventory {

    @Id
    private String id;
    private String storeId;
    private String productId;
    private int quantity;
    private int lowStockThreshold;

    public Inventory() {
    }

    public Inventory(String id, String storeId, String productId, int quantity, int lowStockThreshold) {
        this.id = id;
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    @JsonIgnore
    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }
}