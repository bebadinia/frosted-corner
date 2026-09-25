package com.frostedcorner.recommendations;

import com.frostedcorner.assistant.AssistantRecommendation;
import com.frostedcorner.assistant.CustomerIntent;
import com.frostedcorner.catalog.Product;
import com.frostedcorner.catalog.ProductRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private final ProductRepository productRepository;

    public RecommendationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<AssistantRecommendation> recommend(CustomerIntent intent) {
        return productRepository.findAllByActiveTrue().stream()
            .filter(Product::isActive)
                .map(product -> new ScoredRecommendation(product, score(product, intent)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(ScoredRecommendation::score).reversed()
                        .thenComparing(scored -> scored.product().getName()))
                .limit(3)
                .map(scored -> toRecommendation(scored.product(), intent))
                .toList();
    }

    public List<Product> findActiveProductsByIds(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        Set<String> requestedIds = new LinkedHashSet<>(productIds);
        List<Product> activeProducts = productRepository.findAllById(requestedIds).stream()
                .filter(Product::isActive)
                .collect(Collectors.toMap(Product::getId, product -> product, (left, right) -> left))
                .values()
                .stream()
                .toList();

        return requestedIds.stream()
                .map(productId -> activeProducts.stream()
                        .filter(product -> product.getId().equals(productId))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    public List<String> getActiveCategories() {
        return productRepository.findAllByActiveTrue().stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private AssistantRecommendation toRecommendation(Product product, CustomerIntent intent) {
        return new AssistantRecommendation(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getImageFileName(),
                suggestedQuantity(product, intent.servings()),
                buildReason(product, intent));
    }

    private int score(Product product, CustomerIntent intent) {
        String searchable = searchableText(product);
        int score = 0;

        if (intent.event() != null) {
            score += scoreEvent(product, searchable, intent.event());
        }
        if (intent.flavor() != null && searchable.contains(intent.flavor().toLowerCase(Locale.US))) {
            score += 8;
        }
        if (intent.servings() != null) {
            score += scoreServings(product, intent.servings());
        }
        for (String keyword : intent.keywords()) {
            if (searchable.contains(keyword)) {
                score += 4;
            }
        }

        if (score == 0 && intent.event() == null && intent.flavor() == null && intent.servings() != null) {
            score += scoreServings(product, intent.servings());
        }

        return score;
    }

    private int scoreEvent(Product product, String searchable, String event) {
        String category = product.getCategory() == null ? "" : product.getCategory().toLowerCase(Locale.US);
        return switch (event) {
            case "Birthday" -> {
                int score = 0;
                if (searchable.contains("birthday") || searchable.contains("celebration") || searchable.contains("sprinkles")) {
                    score += 10;
                }
                if (category.contains("cake") || category.contains("cupcake")) {
                    score += 6;
                }
                yield score;
            }
            case "Christmas" -> {
                int score = 0;
                if (category.contains("seasonal")) {
                    score += 10;
                }
                if (searchable.contains("peppermint") || searchable.contains("chocolate")
                        || searchable.contains("cookie") || searchable.contains("cinnamon")) {
                    score += 5;
                }
                yield score;
            }
            case "Party" -> scoreServings(product, 10);
            default -> 0;
        };
    }

    private int scoreServings(Product product, int servings) {
        if (servings <= 0) {
            return 0;
        }

        String category = product.getCategory() == null ? "" : product.getCategory().toLowerCase(Locale.US);
        if (servings >= 8 && (category.contains("cupcake") || category.contains("cookie")
                || category.contains("brownie") || category.contains("cake")
                || category.contains("cheesecake") || category.contains("seasonal"))) {
            return 5;
        }
        return servings >= 4 ? 2 : 1;
    }

    private int suggestedQuantity(Product product, Integer servings) {
        if (servings == null || servings <= 0) {
            return 1;
        }

        String category = product.getCategory() == null ? "" : product.getCategory().toLowerCase(Locale.US);
        if (category.contains("cupcake") || category.contains("cookie")
                || category.contains("brownie") || category.contains("seasonal")) {
            return servings;
        }
        if (category.contains("cake") || category.contains("cheesecake")) {
            return servings > 12 ? 2 : 1;
        }
        return servings;
    }

    private String buildReason(Product product, CustomerIntent intent) {
        List<String> reasons = new java.util.ArrayList<>();
        String searchable = searchableText(product);
        if (intent.event() != null && scoreEvent(product, searchable, intent.event()) > 0) {
            reasons.add("fits the " + intent.event().toLowerCase(Locale.US) + " request");
        }
        if (intent.flavor() != null && searchable.contains(intent.flavor().toLowerCase(Locale.US))) {
            reasons.add("matches the " + intent.flavor().toLowerCase(Locale.US) + " preference");
        }
        if (intent.servings() != null) {
            reasons.add("works for about " + intent.servings() + " people");
        }
        if (reasons.isEmpty()) {
            reasons.add("is available on the current active menu");
        }
        return Character.toUpperCase(reasons.get(0).charAt(0)) + String.join(" and ", reasons).substring(1) + ".";
    }

    private String searchableText(Product product) {
        return String.join(" ",
                        safe(product.getName()),
                        safe(product.getDescription()),
                        safe(product.getCategory()))
                .toLowerCase(Locale.US);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record ScoredRecommendation(Product product, int score) {
    }
}