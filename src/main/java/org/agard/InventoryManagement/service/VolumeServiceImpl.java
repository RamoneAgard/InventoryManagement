package org.agard.InventoryManagement.service;

import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.repositories.VolumeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
@RequiredArgsConstructor
public class VolumeServiceImpl implements VolumeService {

    private final VolumeRepository volumeRepository;

    private final Integer DEFAULT_PAGE_SIZE = 20;

    private final Integer MAX_PAGE_SIZE = 50;

    private final Sort defaultSort = Sort.by("valueCode");


    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){

        if(pageNumber == null || pageNumber < 0){
            pageNumber = 0;
        }

        if(pageSize == null || (pageSize < 1 || pageSize > MAX_PAGE_SIZE)){
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return PageRequest.of(pageNumber, pageSize, defaultSort);
    }

    @Override
    public List<Volume> getAllVolumes() {
        return volumeRepository.findAllByDeletedFalse(defaultSort);
    }

    @Override
    public Page<Volume> filterVolumePage(String description, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        if(!StringUtils.hasText(description)){
            description = null;
        }

        return volumeRepository.findAllByFilter(description, pageRequest);
    }

    @Override
    public Volume getById(Long id) {
        return volumeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> {
                    throw new NotFoundException("Volume not found for ID: " + id);
                });
    }

    @Override
    public void saveVolume(Volume volume) {
        volumeRepository.save(volume);
    }

    @Override
    public void deleteById(Long id) {
        if(volumeRepository.existsById(id)){
            volumeRepository.softDeleteById(id);
            return;
        }
        throw new NotFoundException("Volume not found for ID: " + id);
    }
}
