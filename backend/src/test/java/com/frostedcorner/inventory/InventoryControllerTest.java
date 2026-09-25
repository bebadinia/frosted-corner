package com.frostedcorner.inventory;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.frostedcorner.auth.SecurityConfig;
import com.frostedcorner.auth.StoreAccessService;
import com.frostedcorner.auth.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private StoreAccessService storeAccessService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void rejectsUnauthenticatedInventoryRequests() throws Exception {
        mockMvc.perform(get("/api/inventory").param("storeId", "store1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void listsInventoryUsingDocumentedResponseShape() throws Exception {
        when(inventoryService.getInventory("store1"))
                .thenReturn(List.of(new Inventory("inv1", "store1", "P001", 50, 10)));

        mockMvc.perform(get("/api/inventory").param("storeId", "store1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("inv1"))
                .andExpect(jsonPath("$[0].storeId").value("store1"))
                .andExpect(jsonPath("$[0].productId").value("P001"))
                .andExpect(jsonPath("$[0].quantity").value(50))
                .andExpect(jsonPath("$[0].lowStockThreshold").value(10))
                .andExpect(jsonPath("$[0].lowStock").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void updatesQuantityUsingDocumentedRequestAndResponseShape() throws Exception {
        Inventory updated = new Inventory("inv1", "store1", "P001", 25, 10);
        when(inventoryService.updateQuantity("store1", "P001", 25)).thenReturn(updated);

        mockMvc.perform(put("/api/inventory/P001")
                        .param("storeId", "store1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inv1"))
                .andExpect(jsonPath("$.storeId").value("store1"))
                .andExpect(jsonPath("$.productId").value("P001"))
                .andExpect(jsonPath("$.quantity").value(25))
                .andExpect(jsonPath("$.lowStockThreshold").value(10));
        verify(inventoryService).updateQuantity("store1", "P001", 25);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void rejectsNegativeQuantity() throws Exception {
        mockMvc.perform(put("/api/inventory/P001")
                        .param("storeId", "store1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void rejectsMissingQuantity() throws Exception {
        mockMvc.perform(put("/api/inventory/P001")
                        .param("storeId", "store1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void requiresStoreId() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isBadRequest());
    }
}