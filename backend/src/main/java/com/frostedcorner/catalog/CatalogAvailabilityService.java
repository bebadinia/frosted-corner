package com.frostedcorner.catalog;

import com.frostedcorner.inventory.InventoryService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CatalogAvailabilityService {

    private final InventoryService inventoryService;

    public CatalogAvailabilityService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public List<ProductAvailabilityResponse> getAvailability(String storeId) {
        return inventoryService.getInventory(storeId).stream()
                .map(inventory -> new ProductAvailabilityResponse(
                        inventory.getProductId(),
                        inventory.getQuantity(),
                        inventory.getLowStockThreshold()))
                .toList();
    }
}
