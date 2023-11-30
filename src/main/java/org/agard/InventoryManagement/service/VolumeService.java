package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.domain.Volume;

import java.util.List;

public interface VolumeService {

    List<Volume> getAllVolumes();

    Volume getById(Long id);

    void saveVolume(Volume volume);

    void deleteById(Long id);
}
