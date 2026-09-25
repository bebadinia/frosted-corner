package com.frostedcorner.locations;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class FranchiseLocationController {

    private final FranchiseLocationService franchiseLocationService;

    public FranchiseLocationController(FranchiseLocationService franchiseLocationService) {
        this.franchiseLocationService = franchiseLocationService;
    }

    @GetMapping
    public List<FranchiseLocation> listStores() {
        return franchiseLocationService.listStores();
    }

    @GetMapping("/nearest")
    public LocationDistanceRankingResponse getNearestStore(
            @RequestParam String demoAddressOrZip) {
        return franchiseLocationService.rankStoresByDistance(demoAddressOrZip);
    }
}