package com.frostedcorner.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> getInventory(String storeId) {
        return inventoryRepository.findAllByStoreId(storeId);
    }

    public Inventory updateQuantity(String storeId, String productId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new InvalidInventoryQuantityException();
        }

        Inventory inventory = inventoryRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseThrow(() -> new InventoryNotFoundException(storeId, productId));
        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }

    public List<InventoryDeduction> validateAvailability(
            String storeId, Map<String, Integer> requestedQuantities) {
        List<InventoryDeduction> deductions = new ArrayList<>();

        requestedQuantities.forEach((productId, requestedQuantity) -> {
            Inventory inventory = inventoryRepository.findByStoreIdAndProductId(storeId, productId)
                    .orElseThrow(() -> new InventoryNotFoundException(storeId, productId));
            if (inventory.getQuantity() < requestedQuantity) {
                throw new InsufficientInventoryException(
                        storeId, productId, requestedQuantity, inventory.getQuantity());
            }
            deductions.add(new InventoryDeduction(inventory, requestedQuantity));
        });

        return deductions;
    }

    public void applyDeductions(List<InventoryDeduction> deductions) {
        List<Inventory> updatedInventory = deductions.stream()
                .map(deduction -> {
                    Inventory inventory = deduction.inventory();
                    inventory.setQuantity(inventory.getQuantity() - deduction.quantity());
                    return inventory;
                })
                .toList();
        inventoryRepository.saveAll(updatedInventory);
    }
}