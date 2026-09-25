package com.frostedcorner.catalog;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ProductDataSeeder implements ApplicationRunner {

    private static final List<Product> DEMO_PRODUCTS = List.of(
            new Product("P001", "Classic Chocolate Cake",
                    "Rich chocolate cake layered with smooth chocolate buttercream.",
                    new BigDecimal("32.99"), "Cakes", "classic-chocolate-cake.jpg", true),
            new Product("P002", "Strawberry Shortcake",
                    "Vanilla sponge cake layered with fresh strawberries and whipped cream.",
                    new BigDecimal("34.99"), "Cakes", "strawberry-shortcake.jpg", true),
            new Product("P003", "Red Velvet Cake",
                    "Classic red velvet cake finished with cream cheese frosting.",
                    new BigDecimal("36.99"), "Cakes", "red-velvet-cake.jpg", true),
            new Product("P004", "Vanilla Birthday Cake",
                    "Vanilla celebration cake topped with buttercream and colorful sprinkles.",
                    new BigDecimal("31.99"), "Cakes", "vanilla-birthday-cake.jpg", true),
            new Product("P005", "Chocolate Fudge Cupcake",
                    "Chocolate cupcake topped with rich fudge frosting.",
                    new BigDecimal("4.49"), "Cupcakes", "chocolate-fudge-cupcake.jpg", true),
            new Product("P006", "Vanilla Sprinkle Cupcake",
                    "Vanilla cupcake topped with buttercream frosting and colorful sprinkles.",
                    new BigDecimal("3.99"), "Cupcakes", "vanilla-sprinkle-cupcake.jpg", true),
            new Product("P007", "Red Velvet Cupcake",
                    "Red velvet cupcake topped with cream cheese frosting.",
                    new BigDecimal("4.49"), "Cupcakes", "red-velvet-cupcake.jpg", true),
            new Product("P008", "Chocolate Chip Cookie",
                    "Soft-baked cookie filled with semi-sweet chocolate chips.",
                    new BigDecimal("2.99"), "Cookies", "chocolate-chip-cookie.jpg", true),
            new Product("P009", "Snickerdoodle Cookie",
                    "Soft cinnamon-sugar cookie with a lightly crisp exterior.",
                    new BigDecimal("2.79"), "Cookies", "snickerdoodle-cookie.jpg", true),
            new Product("P010", "Double Chocolate Cookie",
                    "Chocolate cookie packed with chocolate chips for an extra-rich bite.",
                    new BigDecimal("3.29"), "Cookies", "double-chocolate-cookie.jpg", true),
            new Product("P011", "Classic Fudge Brownie",
                    "Dense chocolate brownie with a soft, fudgy center.",
                    new BigDecimal("4.49"), "Brownies", "classic-fudge-brownie.jpg", true),
            new Product("P012", "Salted Caramel Brownie",
                    "Chocolate brownie topped with caramel and a touch of sea salt.",
                    new BigDecimal("4.99"), "Brownies", "salted-caramel-brownie.jpg", true),
            new Product("P013", "Butter Croissant",
                    "Flaky, buttery pastry baked until crisp and golden.",
                    new BigDecimal("3.49"), "Pastries", "butter-croissant.jpg", true),
            new Product("P014", "Chocolate Croissant",
                    "Flaky butter pastry filled with rich chocolate.",
                    new BigDecimal("3.99"), "Pastries", "chocolate-croissant.jpg", true),
            new Product("P015", "Cinnamon Roll",
                    "Soft cinnamon swirl topped with sweet cream cheese icing.",
                    new BigDecimal("4.49"), "Pastries", "cinnamon-roll.jpg", true),
            new Product("P016", "Classic Cheesecake",
                    "Creamy cheesecake with a buttery graham cracker crust.",
                    new BigDecimal("6.99"), "Cheesecakes", "classic-cheesecake.jpg", true),
            new Product("P017", "Strawberry Cheesecake",
                    "Classic cheesecake topped with a sweet strawberry topping.",
                    new BigDecimal("7.49"), "Cheesecakes", "strawberry-cheesecake.jpg", true),
            new Product("P018", "Pumpkin Spice Cupcake",
                    "Pumpkin spice cupcake topped with cinnamon cream cheese frosting.",
                    new BigDecimal("4.49"), "Seasonal", "pumpkin-spice-cupcake.jpg", true),
            new Product("P019", "Caramel Apple Tart",
                    "Buttery tart filled with cinnamon apples and finished with caramel.",
                    new BigDecimal("5.99"), "Seasonal", "caramel-apple-tart.jpg", true),
            new Product("P020", "Peppermint Chocolate Cupcake",
                    "Chocolate cupcake topped with peppermint frosting and chocolate crumbs.",
                    new BigDecimal("4.99"), "Seasonal", "peppermint-chocolate-cupcake.jpg", false)
    );

    private final ProductRepository productRepository;

    public ProductDataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Product> missingProducts = DEMO_PRODUCTS.stream()
                .filter(product -> !productRepository.existsById(product.getId()))
                .toList();

        if (!missingProducts.isEmpty()) {
            productRepository.saveAll(missingProducts);
        }
    }
}