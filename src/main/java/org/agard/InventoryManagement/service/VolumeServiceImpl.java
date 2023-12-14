package org.agard.InventoryManagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.repositories.VolumeRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
@RequiredArgsConstructor
public class VolumeServiceImpl implements VolumeService, PagingService {

    private final VolumeRepository volumeRepository;

    private final Sort defaultSort = Sort.by("valueCode");


    @Override
    public List<Volume> getAllVolumes() {
        return volumeRepository.findAllByDeletedFalse(defaultSort);
    }

    @Override
    public Page<Volume> filterVolumePage(String description, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, defaultSort);

        if(!StringUtils.hasText(description)){
            description = null;
        }

        return volumeRepository.findAllByFilter(description, pageRequest);
    }

    @Override
    public Page<Volume> filterDeletedVolumePage(String description, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, defaultSort);

        if(!StringUtils.hasText(description)){
            description = null;
        }

        return volumeRepository.findAllDeletedByFilter(description, pageRequest);
    }

    @Override
    public Volume getById(Long id) {
        return volumeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> {
                    throw new NotFoundException("Volume not found for ID: " + id);
                });
    }

    @Override
    @Transactional
    public void saveVolume(Volume volume) {
        try{
            volumeRepository.save(volume);
        }
        catch (RuntimeException e){
            String message = "Something went wrong saving this volume";
            if(e.getCause() instanceof ConstraintViolationException){
                message = "Volume descriptions must be unique";
            }
            throw new ItemCreationException(message);
        }
    }

    @Override
    public void deleteById(Long id) {
        if(volumeRepository.existsById(id)){
            volumeRepository.softDeleteById(id);
            return;
        }
        throw new NotFoundException("Volume not found for ID: " + id);
    }

    @Override
    public void activateById(Long id) {
        if(volumeRepository.existsById(id)){
            volumeRepository.reactivateById(id);
            return;
        }
        throw new NotFoundException("Volume not found for ID: " + id);
    }
}
