package com.frostedcorner.assistant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssistantController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssistantService assistantService;

    @Test
    void returnsAssistantResponseUsingDocumentedShape() throws Exception {
        AssistantChatResponse response = new AssistantChatResponse(
                new AssistantInterpretedRequest("RECOMMENDATION", "Birthday", "Chocolate", 10, null,
                        List.of("Chocolate")),
                "Here are a few birthday-friendly chocolate options from the current menu.",
                "ADD_TO_CART",
                new AssistantOffer(true, 10,
                        "You qualify for a 10% birthday offer. Final pricing is calculated by the backend."),
                List.of(new AssistantRecommendation("P005", "Chocolate Fudge Cupcake",
                        new BigDecimal("4.49"), "Cupcakes", "cupcake.jpg", 10,
                        "Matches the chocolate preference and works for about 10 people.")),
                null);
        when(assistantService.chat(any(AssistantChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "c1",
                                  "storeId": "store1",
                                  "message": "Show me something chocolate."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interpretedRequest.intent").value("RECOMMENDATION"))
                .andExpect(jsonPath("$.message").value("Here are a few birthday-friendly chocolate options from the current menu."))
                .andExpect(jsonPath("$.offer.discountPercent").value(10))
                .andExpect(jsonPath("$.recommendations[0].productId").value("P005"))
                .andExpect(jsonPath("$.recommendations[0].price").value(4.49));
    }

    @Test
    void rejectsMissingMessage() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"c1","storeId":"store1"}
                                """))
                .andExpect(status().isBadRequest());
    }
}