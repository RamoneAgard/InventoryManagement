package org.agard.InventoryManagement.BootstrapData;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Product;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.repositories.CategoryRepository;
import org.agard.InventoryManagement.repositories.ProductRepository;
import org.agard.InventoryManagement.repositories.VolumeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@RequiredArgsConstructor
@Component
public final class BootStrapData implements CommandLineRunner {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final VolumeRepository volumeRepository;

    @Override
    public void run(String... args) throws Exception {
        loadProducts();
    }

    private void loadProducts(){
        Category c1 = categoryRepository.save(
                Category.builder()
                    .name("Vodka")
                    .build()
        );

        Category c2 = categoryRepository.save(
                Category.builder()
                    .name("Rum")
                    .build()
        );

        Category c3 = categoryRepository.save(
                Category.builder()
                        .name("Whiskey")
                        .build()
        );

        Category c4 = categoryRepository.save(
                Category.builder()
                        .name("Wheat Ale")
                        .build()
        );


        Volume v1 = volumeRepository.save(
                Volume.builder()
                        .description("50ml")
                        .valueCode(50)
                        .build()
        );

        Volume v2 = volumeRepository.save(
                Volume.builder()
                        .description("375ml")
                        .valueCode(375)
                        .build()
        );

        Volume v3 = volumeRepository.save(
                Volume.builder()
                        .description("6pk 12oz")
                        .valueCode(6012)
                        .build()
        );


        Product p1 = Product.builder()
                .upc("142536475869")
                .name("New Amsterdam Peach")
                .itemCode("va-p50")
                .price(new BigDecimal(80.00))
                .cost(BigDecimal.valueOf(65.00))
                .volume(v1)
                .unitSize(120)
                .stock(45)
                .category(c1)
                .build();


        Product p2 = Product.builder()
                .upc("079685746352")
                .name("Evan Williams Original")
                .itemCode("we-o37")
                .price(BigDecimal.valueOf(90.99))
                .cost(BigDecimal.valueOf(75.45))
                .volume(v2)
                .unitSize(12)
                .stock(25)
                .category(c3)
                .build();

        Product p3 = Product.builder()
                .upc("109284056978")
                .name("Blue Moon")
                .itemCode("ab-w6p")
                .price(BigDecimal.valueOf(31.99))
                .cost(BigDecimal.valueOf(22.50))
                .volume(v3)
                .unitSize(4)
                .stock(30)
                .category(c4)
                .build();


        productRepository.saveAll(Arrays.asList(p1, p2, p3));
        //categoryRepository.saveAll(Arrays.asList(c1, c2, c3, c4));
        //volumeRepository.saveAll(Arrays.asList(v1, v2, v3));
    }

}
