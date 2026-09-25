package com.frostedcorner.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frostedcorner.catalog.Product;
import com.frostedcorner.catalog.ProductRepository;
import com.frostedcorner.locations.FranchiseLocation;
import com.frostedcorner.locations.FranchiseLocationRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class InventoryDataSeederTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private FranchiseLocationRepository locationRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void seedsInventoryForEveryStoreProductCombination() throws Exception {
        when(inventoryRepository.findAll()).thenReturn(List.of());
        when(locationRepository.findAll()).thenReturn(List.of(
                location("store1"), location("store2")));
        when(productRepository.findAll()).thenReturn(List.of(
            product("P001"), product("P007")));
        InventoryDataSeeder seeder = new InventoryDataSeeder(
                inventoryRepository, locationRepository, productRepository);

        seeder.run(new DefaultApplicationArguments());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);
        verify(inventoryRepository).saveAll(inventoryCaptor.capture());
        List<Inventory> inventory = inventoryCaptor.getValue();
        assertEquals(4, inventory.size());
        assertTrue(inventory.stream().allMatch(item -> item.getLowStockThreshold() == 10));
        assertTrue(inventory.stream().anyMatch(Inventory::isLowStock));
    }

    @Test
    void preservesExistingInventoryAndSeedsOnlyMissingCombinations() throws Exception {
        Inventory existing = new Inventory("existing", "store1", "P001", 50, 10);
        when(inventoryRepository.findAll()).thenReturn(List.of(existing));
        when(locationRepository.findAll()).thenReturn(List.of(location("store1")));
        when(productRepository.findAll()).thenReturn(List.of(product("P001"), product("P002")));
        InventoryDataSeeder seeder = new InventoryDataSeeder(
                inventoryRepository, locationRepository, productRepository);

        seeder.run(new DefaultApplicationArguments());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);
        verify(inventoryRepository).saveAll(inventoryCaptor.capture());
        List<Inventory> inventory = inventoryCaptor.getValue();
        assertEquals(1, inventory.size());
        assertEquals("P002", inventory.getFirst().getProductId());
    }

    @Test
    void doesNotWriteWhenAllCombinationsExist() throws Exception {
        Inventory existing = new Inventory("existing", "store1", "P001", 7, 10);
        when(inventoryRepository.findAll()).thenReturn(List.of(existing));
        when(locationRepository.findAll()).thenReturn(List.of(location("store1")));
        when(productRepository.findAll()).thenReturn(List.of(product("P001")));
        InventoryDataSeeder seeder = new InventoryDataSeeder(
                inventoryRepository, locationRepository, productRepository);

        seeder.run(new DefaultApplicationArguments());

        verify(inventoryRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    private FranchiseLocation location(String id) {
        return new FranchiseLocation(id, "Store", "Street", "City", "ST", "00000", "Manager", 0.0, 0.0);
    }

    private Product product(String id) {
        return new Product(id, "Product", "Description", BigDecimal.ONE,
                "Category", "image.jpg", true);
    }
}