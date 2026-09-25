package com.frostedcorner.orders;

public record CreateOrderCustomerRequest(String name,
                                         String email,
                                         String phone,
                                         String street,
                                         String city,
                                         String state,
                                         String zipCode) {
}