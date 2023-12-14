package org.agard.InventoryManagement.repositories;

import org.agard.InventoryManagement.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE (:lastName is null or UPPER(u.lastName) LIKE UPPER(concat('%', :lastName, '%'))) and" +
            "(:role is null or u.role = :role)")
    Page<User> findAllWithFilters(@Param("lastName") String lastName, @Param("role") String role, Pageable pageable);

}
