package com.frostedcorner.catalog;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CatalogAvailabilityService catalogAvailabilityService;

    public ProductController(ProductService productService,
                             CatalogAvailabilityService catalogAvailabilityService) {
        this.productService = productService;
        this.catalogAvailabilityService = catalogAvailabilityService;
    }

    @GetMapping
    public List<Product> getProducts() {
        return productService.getActiveProducts();
    }

    @GetMapping("/availability")
    public List<ProductAvailabilityResponse> getAvailability(@RequestParam String storeId) {
        return catalogAvailabilityService.getAvailability(storeId);
    }
}