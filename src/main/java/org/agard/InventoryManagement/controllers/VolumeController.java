package org.agard.InventoryManagement.controllers;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.domain.Category;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_EDITOR')")
public class VolumeController {

    public static final String VOLUME_TABLE_PATH = "/volumes/table";

    public static final String VOLUME_UPDATE_PATH = "/volumes/update";

    public static final String VOLUME_DELETE_PATH = "/volumes/delete";

    private final VolumeService volumeService;


    @PreAuthorize("hasRole('ROLE_EDITOR')")
    @RequestMapping(value = VOLUME_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getVolumeTable(@RequestParam(required = false, name = "description") String descriptionQuery,
                                 @RequestParam(defaultValue = "0") Integer pageNumber,
                                 @RequestParam(required = false) Integer pageSize,
                                 Model model){

        addPageToModel(descriptionQuery, pageNumber, pageSize, model);

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
            volume = volumeService.getById(id);
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
                                   @RequestParam(required = false, name = "description") String descriptionQuery,
                                   @RequestParam(defaultValue = "0") Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize,
                                   Model model){

        volumeService.deleteById(id);
        addPageToModel(descriptionQuery, pageNumber, pageSize, model);

        return ViewNames.VOLUME_TABLE_FRAGMENT;
    }

    private void addPageToModel(String description,
                                Integer pageNumber,
                                Integer pageSize,
                                Model model){

        Page<Volume> volumePage = volumeService.filterVolumePage(description, pageNumber, pageSize);

        model.addAttribute("volumePage", volumePage);
        model.addAttribute("descriptionQuery", description);
    }

}
