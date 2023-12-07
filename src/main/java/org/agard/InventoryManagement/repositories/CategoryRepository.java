package org.agard.InventoryManagement.repositories;

import jakarta.transaction.Transactional;
import org.agard.InventoryManagement.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByIdAndDeletedFalse(Long id);

    List<Category> findAllByDeletedFalse(Sort sort);

    @Query("SELECT c from Category c WHERE c.deleted = false and" +
            "(:name is null or UPPER(c.name) LIKE UPPER(concat('%', :name, '%')))")
    Page<Category> findAllByFilter(@Param("name") String name, Pageable pageable);

    @Query("SELECT c from Category c WHERE c.deleted = true and" +
            "(:name is null or UPPER(c.name) LIKE UPPER(concat('%', :name, '%')))")
    Page<Category> findAllDeletedByFilter(@Param("name") String name, Pageable pageable);

    @Query("UPDATE Category c SET c.deleted=true WHERE c.id = :id")
    @Modifying
    @Transactional
    void softDeleteById(@Param("id") Long id);

    @Query("UPDATE Category c SET c.deleted=false WHERE c.id = :id")
    @Modifying
    @Transactional
    void reactivateById(@Param("id") Long id);
}
