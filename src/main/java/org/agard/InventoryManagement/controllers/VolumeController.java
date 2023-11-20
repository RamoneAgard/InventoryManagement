package org.agard.InventoryManagement.controllers;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_EDITOR')")
public class VolumeController {

    public static final String VOLUME_TABLE_PATH = "/volumes/table";

    public static final String VOLUME_UPDATE_PATH = "/volumes/update";

    public static final String VOLUME_DELETE_PATH = "/volumes/delete";

    private final VolumeService volumeService;


    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @GetMapping(VOLUME_TABLE_PATH)
    public String getVolumeTable(Model model){

        model.addAttribute("volumes",
                volumeService.getAllVolumes());

        return ViewNames.VOLUME_TABLE_FRAGMENT;
    }

    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @GetMapping(VOLUME_UPDATE_PATH)
    public String getVolumeUpdate(@RequestParam(required = false) Long id,
                                  Model model){
        Volume volume;
        if(id == null){
            volume = new Volume();
        } else {
            volume = volumeService.getById(id).orElseThrow(()-> {
                throw new NotFoundException("Volume not found for ID: " + id);
            });
        }
        model.addAttribute("volume", volume);

        return ViewNames.VOLUME_UPDATE_FRAGMENT;
    }


    @PostMapping(VOLUME_UPDATE_PATH)
    public String processCreateOrUpdate(@Valid Volume volume,
                                        BindingResult bindingResult,
                                        HttpServletResponse response,
                                        Model model){
        if(!bindingResult.hasErrors()){
            volumeService.saveVolume(volume);
            response.setStatus(201);
            model.addAttribute("volume", new Volume());
        }

        return ViewNames.VOLUME_UPDATE_FRAGMENT;
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(VOLUME_DELETE_PATH)
    public String deleteVolumeById(@RequestParam Long id,
                                   Model model){
        if(volumeService.deleteById(id)){
            model.addAttribute("volumes",
                    volumeService.getAllVolumes());
            return ViewNames.VOLUME_TABLE_FRAGMENT;
        }
        throw new NotFoundException("Volume not found for ID: " + id);
    }

}
