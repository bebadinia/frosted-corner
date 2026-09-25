package com.frostedcorner.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class ProductDataSeederTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    void savesDemoProductsThatDoNotAlreadyExist() {
        when(productRepository.existsById(anyString())).thenReturn(false);
        ProductDataSeeder seeder = new ProductDataSeeder(productRepository);

        seeder.run(new DefaultApplicationArguments());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Product>> productsCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRepository).saveAll(productsCaptor.capture());
        assertThat(productsCaptor.getValue())
            .hasSize(20)
                .extracting(Product::getId)
                .doesNotHaveDuplicates();
        assertThat(productsCaptor.getValue())
            .filteredOn(product -> !product.isActive())
            .extracting(Product::getId)
            .containsExactly("P020");
    }

    @Test
    void doesNotDuplicateExistingDemoProducts() {
        when(productRepository.existsById(anyString())).thenReturn(true);
        ProductDataSeeder seeder = new ProductDataSeeder(productRepository);

        seeder.run(new DefaultApplicationArguments());

        verify(productRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }
}