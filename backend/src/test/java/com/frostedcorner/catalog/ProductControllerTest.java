package com.frostedcorner.catalog;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void returnsCatalogUsingDocumentedResponseShape() throws Exception {
        Product product = new Product("p1", "Chocolate Cupcake", "Chocolate cupcake with frosting",
                new BigDecimal("4.50"), "Cupcakes", "chocolate-cupcake.jpg", true);
        when(productService.getActiveProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p1"))
                .andExpect(jsonPath("$[0].name").value("Chocolate Cupcake"))
                .andExpect(jsonPath("$[0].description").value("Chocolate cupcake with frosting"))
                .andExpect(jsonPath("$[0].price").value(4.5))
                .andExpect(jsonPath("$[0].category").value("Cupcakes"))
                .andExpect(jsonPath("$[0].imageFileName").value("chocolate-cupcake.jpg"))
                .andExpect(jsonPath("$[0].active").value(true));
    }
}