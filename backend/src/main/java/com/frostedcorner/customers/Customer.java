package com.frostedcorner.customers;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers")
public class Customer {

    @Id
    private String id;
    private String name;

    @Indexed(unique = true)
    private String email;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private List<String> preferences = List.of();
    private List<String> favoriteCategories = List.of();

    public Customer() {
    }

    public Customer(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}