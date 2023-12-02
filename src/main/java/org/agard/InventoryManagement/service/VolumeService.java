package org.agard.InventoryManagement.service;

import org.agard.InventoryManagement.domain.Volume;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VolumeService {

    List<Volume> getAllVolumes();

    Page<Volume> filterVolumePage(String description, Integer pageNumber, Integer pageSize);

    Volume getById(Long id);

    void saveVolume(Volume volume);

    void deleteById(Long id);
}
