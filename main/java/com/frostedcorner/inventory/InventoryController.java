package com.frostedcorner.inventory;

import com.frostedcorner.auth.StoreAccessService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final StoreAccessService storeAccessService;

    public InventoryController(InventoryService inventoryService,
                               StoreAccessService storeAccessService) {
        this.inventoryService = inventoryService;
        this.storeAccessService = storeAccessService;
    }

    @GetMapping
    public List<Inventory> getInventory(@RequestParam String storeId) {
        storeAccessService.requireInventoryAccess(storeId);
        return inventoryService.getInventory(storeId);
    }

    @PutMapping("/{productId}")
    public Inventory updateQuantity(@PathVariable String productId,
                                    @RequestParam String storeId,
                                    @Valid @RequestBody InventoryQuantityUpdate update) {
                        storeAccessService.requireInventoryAccess(storeId);
        return inventoryService.updateQuantity(storeId, productId, update.quantity());
    }
}