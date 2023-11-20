package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.domain.Volume;

import java.util.List;
import java.util.Optional;

public interface VolumeService {

    List<Volume> getAllVolumes();

    Optional<Volume> getById(Long id);

    void saveVolume(Volume volume);

    boolean deleteById(Long id);
}
