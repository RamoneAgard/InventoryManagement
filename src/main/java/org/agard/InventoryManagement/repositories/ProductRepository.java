package org.agard.InventoryManagement.repositories;

import org.agard.InventoryManagement.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE (:name is null or p.name LIKE %:name%) and " +
            "(:categories is null or p.category.id IN :categories) and " +
            "(:volumes is null or p.volume.id IN :volumes)")
    Page<Product> findAllWithFilters(@Param("name") String name, @Param("categories") List<Long> categories, @Param("volumes") List<Long> volumes, Pageable pageable);

}
