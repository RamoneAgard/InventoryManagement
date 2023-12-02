package org.agard.InventoryManagement.repositories;

import jakarta.transaction.Transactional;
import org.agard.InventoryManagement.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.deleted = false and (:name is null or UPPER(p.name) LIKE UPPER(concat('%',:name, '%'))) and " +
            "(:categories is null or p.category.id IN :categories) and " +
            "(:volumes is null or p.volume.id IN :volumes)")
    Page<Product> findAllWithFilters(@Param("name") String name, @Param("categories") List<Long> categories, @Param("volumes") List<Long> volumes, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = true and (:name is null or UPPER(p.name) LIKE UPPER(concat('%',:name, '%'))) and " +
            "(:categories is null or p.category.id IN :categories) and " +
            "(:volumes is null or p.volume.id IN :volumes)")
    Page<Product> findAllDeletedWithFilters(@Param("name") String name, @Param("categories") List<Long> categories, @Param("volumes") List<Long> volumes, Pageable pageable);

    Product findByItemCodeEqualsIgnoreCaseAndDeletedFalse(String itemCode);

    Optional<Product> findByIdAndDeletedFalse(Long id);

    @Query("UPDATE Product p SET p.deleted=true WHERE p.id = :id")
    @Modifying
    @Transactional
    void softDeleteById(@Param("id") Long id);

    @Query("UPDATE Product p SET p.deleted=false WHERE p.id = :id")
    @Modifying
    @Transactional
    void reactiveById(@Param("id") Long id);

}
