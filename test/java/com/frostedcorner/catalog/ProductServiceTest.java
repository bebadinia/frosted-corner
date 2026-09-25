package com.frostedcorner.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void returnsOnlyActiveProductsFromRepository() {
        Product product = new Product("p1", "Chocolate Cupcake", "Chocolate cupcake with frosting",
                new BigDecimal("4.50"), "Cupcakes", "chocolate-cupcake.jpg", true);
        when(productRepository.findAllByActiveTrue()).thenReturn(List.of(product));

        List<Product> products = productService.getActiveProducts();

        assertThat(products).containsExactly(product);
        verify(productRepository).findAllByActiveTrue();
    }
}