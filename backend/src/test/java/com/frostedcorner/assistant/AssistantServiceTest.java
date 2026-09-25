package com.frostedcorner.assistant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frostedcorner.ai.AiClient;
import com.frostedcorner.catalog.Product;
import com.frostedcorner.inventory.InventoryService;
import com.frostedcorner.recommendations.RecommendationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private AssistantService assistantService;

    @Test
    void returnsValidatedCartActionForReferencedRecommendations() {
        when(aiClient.interpret("Add two of those to my cart."))
                .thenReturn(new CustomerIntent(AssistantIntentType.ADD_TO_CART, null, null,
                        null, 2, null, true, List.of()));
        Product product = new Product("P005", "Chocolate Fudge Cupcake", "Chocolate cupcake",
                new BigDecimal("4.49"), "Cupcakes", "cupcake.jpg", true);
        when(recommendationService.findActiveProductsByIds(List.of("P005"))).thenReturn(List.of(product));

        AssistantChatResponse response = assistantService.chat(
                new AssistantChatRequest("c1", "store1", "Add two of those to my cart.", List.of("P005")));

        assertThat(response.cartAction()).isNotNull();
        assertThat(response.cartAction().items()).hasSize(1);
        assertThat(response.cartAction().items().get(0).quantity()).isEqualTo(2);
        verify(inventoryService).validateAvailability("store1", Map.of("P005", 2));
    }

    @Test
    void asksForClarificationWhenPreviousRecommendationIsMissing() {
        when(aiClient.interpret("Add that to my cart."))
                .thenReturn(new CustomerIntent(AssistantIntentType.ADD_TO_CART, null, null,
                        null, 1, null, false, List.of()));

        AssistantChatResponse response = assistantService.chat(
                new AssistantChatRequest("c1", "store1", "Add that to my cart.", List.of()));

        assertThat(response.cartAction()).isNull();
        assertThat(response.message()).contains("Tell me which recommended dessert");
    }

    @Test
    void returnsGracefulUnsupportedResponse() {
        when(aiClient.interpret("Can you write my taxes?"))
                .thenReturn(new CustomerIntent(AssistantIntentType.UNSUPPORTED, null, null,
                        null, null, null, false, List.of()));

        AssistantChatResponse response = assistantService.chat(
                new AssistantChatRequest("c1", "store1", "Can you write my taxes?", List.of()));

        assertThat(response.message()).contains("menu recommendations");
        assertThat(response.recommendations()).isEmpty();
    }
}