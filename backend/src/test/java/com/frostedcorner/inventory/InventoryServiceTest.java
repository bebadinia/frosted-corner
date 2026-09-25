package com.frostedcorner.inventory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void listsInventoryForStore() {
        List<Inventory> inventory = List.of(new Inventory("inv1", "store1", "P001", 50, 10));
        when(inventoryRepository.findAllByStoreId("store1")).thenReturn(inventory);

        List<Inventory> result = inventoryService.getInventory("store1");

        assertSame(inventory, result);
        verify(inventoryRepository).findAllByStoreId("store1");
    }

    @Test
    void updatesAndPersistsQuantity() {
        Inventory inventory = new Inventory("inv1", "store1", "P001", 50, 10);
        when(inventoryRepository.findByStoreIdAndProductId("store1", "P001"))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        Inventory result = inventoryService.updateQuantity("store1", "P001", 25);

        assertSame(inventory, result);
        assertFalse(result.isLowStock());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void quantityAtThresholdIsLowStock() {
        Inventory inventory = new Inventory("inv1", "store1", "P001", 50, 10);
        when(inventoryRepository.findByStoreIdAndProductId("store1", "P001"))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        Inventory result = inventoryService.updateQuantity("store1", "P001", 10);

        assertTrue(result.isLowStock());
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThrows(InvalidInventoryQuantityException.class,
                () -> inventoryService.updateQuantity("store1", "P001", -1));
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void rejectsMissingQuantity() {
        assertThrows(InvalidInventoryQuantityException.class,
                () -> inventoryService.updateQuantity("store1", "P001", null));
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void rejectsUpdateWhenInventoryDoesNotExist() {
        when(inventoryRepository.findByStoreIdAndProductId("store1", "P999"))
                .thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> inventoryService.updateQuantity("store1", "P999", 5));
    }

        @Test
        void validatesAvailabilityWithoutChangingInventory() {
        Inventory inventory = new Inventory("inv1", "store1", "P001", 20, 10);
        when(inventoryRepository.findByStoreIdAndProductId("store1", "P001"))
            .thenReturn(Optional.of(inventory));

        List<InventoryDeduction> deductions = inventoryService.validateAvailability(
            "store1", Map.of("P001", 3));

        assertEquals(1, deductions.size());
        assertSame(inventory, deductions.getFirst().inventory());
        assertEquals(3, deductions.getFirst().quantity());
        assertEquals(20, inventory.getQuantity());
        verify(inventoryRepository, never()).saveAll(anyList());
        }

        @Test
        void rejectsInsufficientInventoryWithoutSaving() {
        Inventory inventory = new Inventory("inv1", "store1", "P001", 2, 10);
        when(inventoryRepository.findByStoreIdAndProductId("store1", "P001"))
            .thenReturn(Optional.of(inventory));

        assertThrows(InsufficientInventoryException.class,
            () -> inventoryService.validateAvailability("store1", Map.of("P001", 3)));

        assertEquals(2, inventory.getQuantity());
        verify(inventoryRepository, never()).saveAll(anyList());
        }

        @Test
        void missingInventoryPreventsAvailabilityValidation() {
        when(inventoryRepository.findByStoreIdAndProductId("store1", "P999"))
            .thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
            () -> inventoryService.validateAvailability("store1", Map.of("P999", 1)));

        verify(inventoryRepository, never()).saveAll(anyList());
        }

        @Test
        void appliesAndPersistsInventoryDeductions() {
        Inventory first = new Inventory("inv1", "store1", "P001", 20, 10);
        Inventory second = new Inventory("inv2", "store1", "P005", 8, 10);

        inventoryService.applyDeductions(List.of(
            new InventoryDeduction(first, 3),
            new InventoryDeduction(second, 2)));

        assertEquals(17, first.getQuantity());
        assertEquals(6, second.getQuantity());
        verify(inventoryRepository).saveAll(List.of(first, second));
        }
}