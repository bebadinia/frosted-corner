package com.frostedcorner.recommendations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.frostedcorner.assistant.AssistantIntentType;
import com.frostedcorner.assistant.AssistantRecommendation;
import com.frostedcorner.assistant.CustomerIntent;
import com.frostedcorner.catalog.Product;
import com.frostedcorner.catalog.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void returnsBirthdayChocolateRecommendationsFromRealProducts() {
        when(productRepository.findAllByActiveTrue()).thenReturn(List.of(
                product("P004", "Vanilla Birthday Cake", "Vanilla celebration cake", "31.99", "Cakes", true),
                product("P005", "Chocolate Fudge Cupcake", "Chocolate cupcake topped with rich fudge frosting.", "4.49", "Cupcakes", true),
                product("P020", "Peppermint Chocolate Cupcake", "Holiday cupcake", "4.99", "Seasonal", false)));

        List<AssistantRecommendation> recommendations = recommendationService.recommend(
                new CustomerIntent(AssistantIntentType.RECOMMENDATION, "Birthday", "Chocolate",
                        10, null, null, false, List.of("cupcake")));

        assertThat(recommendations).extracting(AssistantRecommendation::productId)
                .containsExactly("P005", "P004");
        assertThat(recommendations.get(0).suggestedQuantity()).isEqualTo(10);
    }

    @Test
    void ranksBirthdayCakeFirstForTheDemoPartyRequest() {
        when(productRepository.findAllByActiveTrue()).thenReturn(List.of(
                product("P004", "Vanilla Birthday Cake",
                        "Vanilla celebration cake topped with buttercream and colorful sprinkles.",
                        "31.99", "Cakes", true),
                product("P006", "Vanilla Sprinkle Cupcake",
                        "Vanilla cupcake topped with colorful sprinkles.",
                        "3.99", "Cupcakes", true),
                product("P001", "Classic Chocolate Cake",
                        "Rich chocolate cake.", "32.99", "Cakes", true)));

        List<AssistantRecommendation> recommendations = recommendationService.recommend(
                new CustomerIntent(
                        AssistantIntentType.RECOMMENDATION,
                        "Birthday",
                        null,
                        10,
                        null,
                        null,
                        false,
                        List.of("dessert")));

        assertThat(recommendations).extracting(AssistantRecommendation::productId)
                .containsExactly("P004", "P006", "P001");
    }

    @Test
    void returnsChristmasRecommendationsFromActiveCatalogOnly() {
        when(productRepository.findAllByActiveTrue()).thenReturn(List.of(
                product("P018", "Pumpkin Spice Cupcake", "Seasonal cupcake", "4.49", "Seasonal", true),
                product("P019", "Caramel Apple Tart", "Seasonal tart", "5.99", "Seasonal", true),
                product("P010", "Double Chocolate Cookie", "Chocolate cookie", "3.29", "Cookies", true)));

        List<AssistantRecommendation> recommendations = recommendationService.recommend(
                new CustomerIntent(AssistantIntentType.RECOMMENDATION, "Christmas", null,
                        null, null, null, false, List.of()));

        assertThat(recommendations).extracting(AssistantRecommendation::productId)
                .containsExactly("P019", "P018", "P010");
    }

    private Product product(String id, String name, String description, String price,
                            String category, boolean active) {
        return new Product(id, name, description, new BigDecimal(price), category, "image.jpg", active);
    }
}