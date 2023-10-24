package org.agard.InventoryManagement.BootstrapData;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.repositories.CategoryRepository;
import org.agard.InventoryManagement.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@RequiredArgsConstructor
@Component
public final class BootStrapData implements CommandLineRunner {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        
        loadProducts();
    }

    private void loadProducts(){
        Category c1 = categoryRepository.save(
                Category.builder()
                    .name("Accessory")
                    .build()
        );

        Category c2 = categoryRepository.save(
                Category.builder()
                    .name("Miscellaneous")
                    .build()
        );

        Category c3 = categoryRepository.save(
                Category.builder()
                        .name("Electronic")
                        .build()
        );


        Product p1 = Product.builder()
                .name("100W Light Bulbs")
                .price(new BigDecimal(4.50))
                .cost(BigDecimal.valueOf(2.50))
                .stock(45)
                .category(c2)
                .build();


        Product p2 = Product.builder()
                .name("IPhone 12 Phone Case")
                .price(BigDecimal.valueOf(10.99))
                .cost(BigDecimal.valueOf(4.45))
                .stock(32)
                .category(c1)
                .build();

        productRepository.saveAll(Arrays.asList(p1, p2));
        categoryRepository.saveAll(Arrays.asList(c1, c2));
    }

}
