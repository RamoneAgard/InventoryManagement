package org.agard.InventoryManagement.repositories;

import org.agard.InventoryManagement.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByNameIsLikeIgnoreCase(String name, Pageable pageable);

    Page<Product> findAllByCategoryNameOrderByCategory(String categoryName, Pageable pageable);

    Page<Product> findAllByNameIsLikeIgnoreCaseAndCategoryNameOrderByCategory(String name, String categoryName, Pageable pageable);
}
