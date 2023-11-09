package org.agard.InventoryManagement.repositories;

import org.agard.InventoryManagement.domain.Volume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolumeRepository extends JpaRepository<Volume, Long> {
}
