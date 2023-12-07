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

    public static final String VOLUME_REACTIVATE_PATH = "/volumes/reactivate";

    private final VolumeService volumeService;


    @RequestMapping(value = VOLUME_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getVolumeTable(@RequestParam(required = false, name = "description") String descriptionQuery,
                                 @RequestParam(defaultValue = "0") Integer pageNumber,
                                 @RequestParam(required = false) Integer pageSize,
                                 Model model){

        addPageToModel(descriptionQuery, pageNumber, pageSize, model);

        return ViewNames.VOLUME_TABLE_FRAGMENT;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = VOLUME_TABLE_PATH, params = "deleted", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDeletedVolumeTable(@RequestParam(required = false, name = "description") String descriptionQuery,
                                 @RequestParam(defaultValue = "0") Integer pageNumber,
                                 @RequestParam(required = false) Integer pageSize,
                                 @RequestParam(name = "deleted") Boolean deleted,
                                 Model model){

        if(deleted){
            addDeletedPageToModel(descriptionQuery, pageNumber, pageSize, model);
            return ViewNames.VOLUME_TABLE_FRAGMENT;
        }

        addPageToModel(descriptionQuery, pageNumber, pageSize, model);

        return ViewNames.VOLUME_TABLE_FRAGMENT;
    }


    @GetMapping(VOLUME_UPDATE_PATH)
    public String getVolumeUpdate(@RequestParam(required = false) Long id,
                                  Model model){
        Volume volume;
        if(id == null){
            volume = new Volume();
        } else {
            try{
                volume = volumeService.getById(id);
            }
            catch (NotFoundException e){
                model.addAttribute("addError", e.getMessage());
                volume = new Volume();
            }

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
            try{
                volumeService.saveVolume(volume);
                response.setStatus(201);
                model.addAttribute("volume", new Volume());
            }
            catch (NotFoundException e){
                model.addAttribute("addError", e.getMessage());
            }
        }
        else {
            model.addAttribute("addError",
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
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

        try{
            volumeService.deleteById(id);
        }
        catch (NotFoundException e){
            model.addAttribute("tableError", e.getMessage());
        }

        addPageToModel(descriptionQuery, pageNumber, pageSize, model);

        return ViewNames.VOLUME_TABLE_FRAGMENT;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(VOLUME_REACTIVATE_PATH)
    public String reactivateVolumeById(@RequestParam Long id,
                                   @RequestParam(required = false, name = "description") String descriptionQuery,
                                   @RequestParam(defaultValue = "0") Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize,
                                   Model model){

        try{
            volumeService.activateById(id);
        }
        catch (NotFoundException e){
            model.addAttribute("tableError", e.getMessage());
        }

        addDeletedPageToModel(descriptionQuery, pageNumber, pageSize, model);

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

    private void addDeletedPageToModel(String description,
                                       Integer pageNumber,
                                       Integer pageSize,
                                       Model model){

        Page<Volume> volumePage = volumeService.filterDeletedVolumePage(description, pageNumber, pageSize);

        model.addAttribute("volumePage", volumePage);
        model.addAttribute("descriptionQuery", description);
        model.addAttribute("deletedQuery", "true");
    }
}
