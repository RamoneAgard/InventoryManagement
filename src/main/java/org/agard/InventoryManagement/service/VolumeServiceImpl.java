package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.repositories.VolumeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class VolumeServiceImpl implements VolumeService {

    private final VolumeRepository volumeRepository;

    private final Sort defaultSort = Sort.by("valueCode");

    @Override
    public List<Volume> getAllVolumes() {
        return volumeRepository.findAll(defaultSort);
    }

    @Override
    public Volume getById(Long id) {
        return volumeRepository.findById(id)
                .orElse(null);
    }

    @Override
    public void saveVolume(Volume volume) {
        volumeRepository.save(volume);
    }

    @Override
    public boolean deleteById(Long id) {
        if(volumeRepository.existsById(id)){
            volumeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
