package com.frostedcorner.assistant;

import com.frostedcorner.ai.AiClient;
import com.frostedcorner.catalog.Product;
import com.frostedcorner.inventory.InsufficientInventoryException;
import com.frostedcorner.inventory.InventoryNotFoundException;
import com.frostedcorner.inventory.InventoryService;
import com.frostedcorner.recommendations.RecommendationService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private static final String DEFAULT_STORE_ID = "store1";
    private static final String HELP_MESSAGE = "I'm currently able to help with menu recommendations, events, flavors, and adding recommended desserts to your cart.";

    private final AiClient aiClient;
    private final RecommendationService recommendationService;
    private final InventoryService inventoryService;

    public AssistantService(AiClient aiClient, RecommendationService recommendationService,
                            InventoryService inventoryService) {
        this.aiClient = aiClient;
        this.recommendationService = recommendationService;
        this.inventoryService = inventoryService;
    }

    public AssistantChatResponse chat(AssistantChatRequest request) {
        CustomerIntent intent = aiClient.interpret(request.message());
        AssistantInterpretedRequest interpretedRequest = toInterpretedRequest(intent);

        return switch (intent.intentType()) {
            case RECOMMENDATION -> recommend(intent, interpretedRequest);
            case ADD_TO_CART -> addToCart(request, intent, interpretedRequest);
            case FAQ -> answerFaq(intent, interpretedRequest);
            case UNSUPPORTED -> fallback(interpretedRequest, HELP_MESSAGE);
        };
    }

    private AssistantChatResponse recommend(CustomerIntent intent,
                                            AssistantInterpretedRequest interpretedRequest) {
        List<AssistantRecommendation> recommendations = recommendationService.recommend(intent);
        if (recommendations.isEmpty()) {
            return fallback(interpretedRequest,
                    "I couldn't find a matching active dessert on the current menu. " + HELP_MESSAGE);
        }

        AssistantOffer offer = buildOffer(intent);
        String message = buildRecommendationMessage(intent, recommendations.size());
        return new AssistantChatResponse(
                interpretedRequest,
                message,
                "ADD_TO_CART",
                offer,
                recommendations,
                null);
    }

    private AssistantChatResponse addToCart(AssistantChatRequest request, CustomerIntent intent,
                                            AssistantInterpretedRequest interpretedRequest) {
        List<String> recommendedProductIds = request.recommendedProductIds() == null
                ? List.of()
                : request.recommendedProductIds();
        if (recommendedProductIds.isEmpty()) {
            return fallback(interpretedRequest,
                    "Tell me which recommended dessert you want to add, and I will validate it before updating your cart.");
        }

        List<Product> referencedProducts = resolveReferencedProducts(intent, recommendedProductIds);
        if (referencedProducts.isEmpty()) {
            return fallback(interpretedRequest,
                    "I couldn't safely identify which recommended dessert to add. Please pick one of the latest recommendations by name.");
        }

        int quantity = intent.quantity() == null || intent.quantity() <= 0 ? 1 : intent.quantity();
        String storeId = request.storeId() == null || request.storeId().isBlank()
                ? DEFAULT_STORE_ID
                : request.storeId();
        Map<String, Integer> requestedQuantities = new LinkedHashMap<>();
        referencedProducts.forEach(product -> requestedQuantities.put(product.getId(), quantity));

        try {
            inventoryService.validateAvailability(storeId, requestedQuantities);
        } catch (InsufficientInventoryException | InventoryNotFoundException exception) {
            return fallback(interpretedRequest,
                    "I found the dessert, but there is not enough inventory at " + storeId
                            + " for that request right now.");
        }

        List<AssistantCartItem> items = referencedProducts.stream()
                .map(product -> new AssistantCartItem(product.getId(), product.getName(),
                        product.getPrice(), product.getCategory(), product.getImageFileName(), quantity))
                .toList();
        String message = buildCartMessage(items, quantity);

        return new AssistantChatResponse(
                interpretedRequest,
                message,
                "ADD_TO_CART",
                null,
                List.of(),
                new AssistantCartAction("ADD_VALIDATED_ITEMS", true, items));
    }

    private AssistantChatResponse answerFaq(CustomerIntent intent,
                                            AssistantInterpretedRequest interpretedRequest) {
        String message = switch (intent.faqIntent()) {
            case "MENU" -> "Today's active menu includes "
                    + String.join(", ", recommendationService.getActiveCategories()) + ".";
            case "ORDERING" -> "Share an event, flavor, or party size, and I will recommend active desserts that you can add to your cart.";
            case "FULFILLMENT" -> "For this MVP, the guided assistant supports building your cart and demo checkout through the normal ordering flow.";
            case "HELP" -> HELP_MESSAGE;
            default -> HELP_MESSAGE;
        };

        return new AssistantChatResponse(interpretedRequest, message, "NONE", null, List.of(), null);
    }

    private AssistantChatResponse fallback(AssistantInterpretedRequest interpretedRequest,
                                           String message) {
        return new AssistantChatResponse(interpretedRequest, message, "NONE", null, List.of(), null);
    }

    private List<Product> resolveReferencedProducts(CustomerIntent intent, List<String> recommendedProductIds) {
        List<Product> recommendedProducts = recommendationService.findActiveProductsByIds(recommendedProductIds);
        if (recommendedProducts.isEmpty()) {
            return List.of();
        }

        if (!intent.keywords().isEmpty()) {
            List<Product> keywordMatches = recommendedProducts.stream()
                    .filter(product -> matchesAnyKeyword(product, intent.keywords()))
                    .toList();
            if (keywordMatches.size() == 1) {
                return keywordMatches;
            }
            if (!keywordMatches.isEmpty() && intent.addAllReferencedRecommendations()) {
                return keywordMatches;
            }
            if (!keywordMatches.isEmpty()
                    && intent.keywords().size() == 1
                    && "cake".equals(intent.keywords().getFirst())) {
                return List.of(keywordMatches.getFirst());
            }
        }

        if (intent.addAllReferencedRecommendations()) {
            return recommendedProducts;
        }

        return recommendedProducts.size() == 1 ? recommendedProducts : List.of();
    }

    private boolean matchesAnyKeyword(Product product, List<String> keywords) {
        String searchable = String.join(" ",
                        Objects.toString(product.getName(), ""),
                        Objects.toString(product.getDescription(), ""),
                        Objects.toString(product.getCategory(), ""))
                .toLowerCase(Locale.US);
        return keywords.stream().anyMatch(keyword -> matchesKeyword(product, searchable, keyword));
    }

    private boolean matchesKeyword(Product product, String searchable, String keyword) {
        if ("cake".equals(keyword)) {
            String category = Objects.toString(product.getCategory(), "");
            String name = Objects.toString(product.getName(), "").toLowerCase(Locale.US);
            return "cakes".equalsIgnoreCase(category)
                    || name.matches(".*\\bcake\\b.*");
        }
        return searchable.contains(keyword);
    }

    private AssistantOffer buildOffer(CustomerIntent intent) {
        if (!"Birthday".equals(intent.event())) {
            return null;
        }

        return new AssistantOffer(true, 10,
                "You qualify for a 10% birthday offer. Final pricing is calculated by the backend.");
    }

    private AssistantInterpretedRequest toInterpretedRequest(CustomerIntent intent) {
        return new AssistantInterpretedRequest(
                intent.intentType().name(),
                intent.event(),
                intent.flavor(),
                intent.servings(),
                intent.quantity(),
                intent.flavor() == null ? List.of() : List.of(intent.flavor()));
    }

    private String buildRecommendationMessage(CustomerIntent intent, int recommendationCount) {
        StringBuilder builder = new StringBuilder("Here ");
        builder.append(recommendationCount == 1 ? "is " : "are ");
        if (intent.event() != null) {
            builder.append("a few ").append(intent.event().toLowerCase(Locale.US)).append("-friendly ");
        }
        if (intent.flavor() != null) {
            builder.append(intent.flavor().toLowerCase(Locale.US)).append(' ');
        }
        builder.append(recommendationCount == 1 ? "option" : "options");
        if (intent.servings() != null) {
            builder.append(" for about ").append(intent.servings()).append(" people");
        }
        builder.append(" from the current menu.");
        return builder.toString().replace("a few option", "an option");
    }

    private String buildCartMessage(List<AssistantCartItem> items, int quantity) {
        String productNames = items.stream().map(AssistantCartItem::name).toList().toString()
                .replace("[", "").replace("]", "");
        return "I validated " + quantity + " of " + productNames
                + " against current inventory and it is ready for your cart.";
    }
}