package com.frostedcorner.locations;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "franchiseLocations")
public class FranchiseLocation {

    @Id
    private String id;
    private String storeName;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String managerName;
    private double latitude;
    private double longitude;

    public FranchiseLocation() {
    }

    public FranchiseLocation(String id, String storeName, String street, String city,
                             String state, String zipCode, String managerName,
                             double latitude, double longitude) {
        this.id = id;
        this.storeName = storeName;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.managerName = managerName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}