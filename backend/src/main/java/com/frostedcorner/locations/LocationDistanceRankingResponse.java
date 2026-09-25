package com.frostedcorner.locations;

import java.util.List;

public record LocationDistanceRankingResponse(DemoCustomerCoordinates customerLocation,
                                              FranchiseLocationDistanceResponse nearestStore,
                                              List<FranchiseLocationDistanceResponse> stores) {
}