package org.agard.InventoryManagement.repositories;

import jakarta.transaction.Transactional;
import org.agard.InventoryManagement.domain.Volume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VolumeRepository extends JpaRepository<Volume, Long> {

    Optional<Volume> findByIdAndDeletedFalse(Long id);

    List<Volume> findAllByDeletedFalse(Sort sort);

    @Query("SELECT v from Volume v WHERE v.deleted = false and" +
            "(:description is null or UPPER(v.description) LIKE UPPER(concat('%', :description, '%')))")
    Page<Volume> findAllByFilter(@Param("description") String description, Pageable pageable);

    @Query("SELECT v from Volume v WHERE v.deleted = true and" +
            "(:description is null or UPPER(v.description) LIKE UPPER(concat('%', :description, '%')))")
    Page<Volume> findAllDeletedByFilter(@Param("description") String description, Pageable pageable);

    @Query("UPDATE Volume v SET v.deleted=true WHERE v.id = :id")
    @Modifying
    @Transactional
    void softDeleteById(@Param("id") Long id);

    @Query("UPDATE Volume v SET v.deleted=false WHERE v.id = :id")
    @Modifying
    @Transactional
    void reactivateById(@Param("id") Long id);
}
