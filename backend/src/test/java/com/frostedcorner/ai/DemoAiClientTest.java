package com.frostedcorner.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.frostedcorner.assistant.AssistantIntentType;
import com.frostedcorner.assistant.CustomerIntent;
import org.junit.jupiter.api.Test;

class DemoAiClientTest {

    private final DemoAiClient demoAiClient = new DemoAiClient();

    @Test
    void detectsBirthdayFlavorAndServingCount() {
        CustomerIntent intent = demoAiClient.interpret(
                "I need desserts for a birthday party for 10 people and prefer chocolate.");

        assertThat(intent.intentType()).isEqualTo(AssistantIntentType.RECOMMENDATION);
        assertThat(intent.event()).isEqualTo("Birthday");
        assertThat(intent.flavor()).isEqualTo("Chocolate");
        assertThat(intent.servings()).isEqualTo(10);
    }

    @Test
    void detectsDeterministicAddToCartQuantity() {
        CustomerIntent intent = demoAiClient.interpret("Add two of those to my cart.");

        assertThat(intent.intentType()).isEqualTo(AssistantIntentType.ADD_TO_CART);
        assertThat(intent.quantity()).isEqualTo(2);
        assertThat(intent.addAllReferencedRecommendations()).isTrue();
    }

    @Test
    void detectsTheDemoCakeAddCommand() {
        CustomerIntent intent = demoAiClient.interpret("Add the cake to my cart.");

        assertThat(intent.intentType()).isEqualTo(AssistantIntentType.ADD_TO_CART);
        assertThat(intent.quantity()).isEqualTo(1);
        assertThat(intent.keywords()).containsExactly("cake");
    }

    @Test
    void detectsFaqIntent() {
        CustomerIntent intent = demoAiClient.interpret("How does ordering work?");

        assertThat(intent.intentType()).isEqualTo(AssistantIntentType.FAQ);
        assertThat(intent.faqIntent()).isEqualTo("ORDERING");
    }
}