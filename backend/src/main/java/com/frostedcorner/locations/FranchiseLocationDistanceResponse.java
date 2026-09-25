package com.frostedcorner.locations;

public record FranchiseLocationDistanceResponse(String id,
                                                String storeName,
                                                String street,
                                                String city,
                                                String state,
                                                String zipCode,
                                                String managerName,
                                                double latitude,
                                                double longitude,
                                                double distanceMiles) {
}