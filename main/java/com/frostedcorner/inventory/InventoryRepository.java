package com.frostedcorner.inventory;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryRepository extends MongoRepository<Inventory, String> {

    List<Inventory> findAllByStoreId(String storeId);

    Optional<Inventory> findByStoreIdAndProductId(String storeId, String productId);
}