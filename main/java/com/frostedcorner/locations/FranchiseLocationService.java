package com.frostedcorner.locations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FranchiseLocationService {

    private static final double EARTH_RADIUS_MILES = 3958.7613;
    private static final int DISTANCE_SCALE = 2;

    private final FranchiseLocationRepository locationRepository;
    private final DemoCustomerLocationResolver demoCustomerLocationResolver;

    public FranchiseLocationService(FranchiseLocationRepository locationRepository,
                                    DemoCustomerLocationResolver demoCustomerLocationResolver) {
        this.locationRepository = locationRepository;
        this.demoCustomerLocationResolver = demoCustomerLocationResolver;
    }

    public LocationDistanceRankingResponse rankStoresByDistance(String demoAddressOrZip) {
        DemoCustomerCoordinates customerLocation = demoCustomerLocationResolver.resolve(demoAddressOrZip);

        List<FranchiseLocationDistanceResponse> rankedStores = locationRepository.findAll().stream()
                .map(location -> toDistanceResponse(location, customerLocation))
                .sorted(Comparator.comparingDouble(FranchiseLocationDistanceResponse::distanceMiles)
                        .thenComparing(FranchiseLocationDistanceResponse::id))
                .toList();

        if (rankedStores.isEmpty()) {
            throw new InvalidLocationLookupException("No franchise locations are available");
        }

        return new LocationDistanceRankingResponse(customerLocation, rankedStores.getFirst(), rankedStores);
    }

        public List<FranchiseLocation> listStores() {
                return locationRepository.findAll().stream()
                                .sorted(Comparator.comparing(FranchiseLocation::getStoreName)
                                                .thenComparing(FranchiseLocation::getId))
                                .toList();
        }

    private FranchiseLocationDistanceResponse toDistanceResponse(FranchiseLocation location,
                                                                 DemoCustomerCoordinates customerLocation) {
        double distanceMiles = roundToMiles(haversineMiles(customerLocation.latitude(),
                customerLocation.longitude(), location.getLatitude(), location.getLongitude()));

        return new FranchiseLocationDistanceResponse(location.getId(),
                location.getStoreName(),
                location.getStreet(),
                location.getCity(),
                location.getState(),
                location.getZipCode(),
                location.getManagerName(),
                location.getLatitude(),
                location.getLongitude(),
                distanceMiles);
    }

    private double haversineMiles(double startLatitude, double startLongitude,
                                  double endLatitude, double endLongitude) {
        double latitudeDelta = Math.toRadians(endLatitude - startLatitude);
        double longitudeDelta = Math.toRadians(endLongitude - startLongitude);
        double startLatitudeRadians = Math.toRadians(startLatitude);
        double endLatitudeRadians = Math.toRadians(endLatitude);

        double a = Math.pow(Math.sin(latitudeDelta / 2), 2)
                + Math.cos(startLatitudeRadians) * Math.cos(endLatitudeRadians)
                * Math.pow(Math.sin(longitudeDelta / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_MILES * c;
    }

    private double roundToMiles(double distanceMiles) {
        return BigDecimal.valueOf(distanceMiles)
                .setScale(DISTANCE_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }
}