package com.frostedcorner.locations;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FranchiseLocationRepository extends MongoRepository<FranchiseLocation, String> {
}