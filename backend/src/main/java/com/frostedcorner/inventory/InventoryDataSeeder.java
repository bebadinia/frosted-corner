package com.frostedcorner.inventory;

import com.frostedcorner.catalog.Product;
import com.frostedcorner.catalog.ProductRepository;
import com.frostedcorner.locations.FranchiseLocation;
import com.frostedcorner.locations.FranchiseLocationRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class InventoryDataSeeder implements ApplicationRunner {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final InventoryRepository inventoryRepository;
    private final FranchiseLocationRepository locationRepository;
    private final ProductRepository productRepository;

    public InventoryDataSeeder(InventoryRepository inventoryRepository,
                               FranchiseLocationRepository locationRepository,
                               ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.locationRepository = locationRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Set<String> existingStoreProducts = new HashSet<>();
        inventoryRepository.findAll().forEach(inventory -> existingStoreProducts.add(
                storeProductKey(inventory.getStoreId(), inventory.getProductId())));

        List<Product> products = productRepository.findAll();
        List<Inventory> missingInventory = locationRepository.findAll().stream()
                .flatMap(location -> products.stream()
                        .filter(product -> !existingStoreProducts.contains(
                                storeProductKey(location.getId(), product.getId())))
                        .map(product -> createInventory(location, product)))
                .toList();

        if (!missingInventory.isEmpty()) {
            inventoryRepository.saveAll(missingInventory);
        }
    }

    private Inventory createInventory(FranchiseLocation location, Product product) {
        int storeNumber = Integer.parseInt(location.getId().substring("store".length()));
        int productNumber = Integer.parseInt(product.getId().substring(1));
        int simulatedQuantity = (storeNumber * 7 + productNumber * 11) % 76;
        String inventoryId = "INV-" + location.getId() + "-" + product.getId();
        return new Inventory(inventoryId, location.getId(), product.getId(),
                simulatedQuantity, LOW_STOCK_THRESHOLD);
    }

    private String storeProductKey(String storeId, String productId) {
        return storeId + ":" + productId;
    }
}