package com.frostedcorner.orders;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findAllByCustomerIdOrderByCreatedAtDesc(String customerId);
}
