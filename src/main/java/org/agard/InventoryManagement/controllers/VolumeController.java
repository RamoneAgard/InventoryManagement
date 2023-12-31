package org.agard.InventoryManagement.controllers;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agard.InventoryManagement.Exceptions.ItemCreationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.annotations.IsAdmin;
import org.agard.InventoryManagement.annotations.IsEditor;
import org.agard.InventoryManagement.domain.Volume;
import org.agard.InventoryManagement.service.VolumeService;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@IsEditor
public class VolumeController {

    public static final String VOLUME_BASE_PATH = "/volumes";

    public static final String VOLUME_TABLE_PATH = VOLUME_BASE_PATH + "/table";

    public static final String VOLUME_UPDATE_PATH = VOLUME_BASE_PATH + "/update";

    public static final String VOLUME_DELETE_PATH = VOLUME_BASE_PATH + "/delete";

    public static final String VOLUME_REACTIVATE_PATH = VOLUME_BASE_PATH + "/reactivate";

    private final VolumeService volumeService;


    @RequestMapping(value = VOLUME_TABLE_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    public String getVolumeTable(@RequestParam(required = false, name = "description") String descriptionQuery,
                                 @RequestParam(defaultValue = "0") Integer pageNumber,
                                 @RequestParam(required = false) Integer pageSize,
                                 Model model){

        addPageToModel(descriptionQuery, pageNumber, pageSize, model);

        return ViewNames.VOLUME_TABLE_FRAGMENT;
    }

    @IsAdmin
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
            catch (NotFoundException | ItemCreationException e){
                model.addAttribute("addError", e.getMessage());
            }
            catch (RuntimeException e){
                model.addAttribute("addError", "Something went wrong, reload and try again");
            }
        }
        else {
            model.addAttribute("addError",
                    bindingResult.getFieldErrors().get(0).getField() + ": " +
                            bindingResult.getFieldErrors().get(0).getDefaultMessage()
            );
        }

        return ViewNames.VOLUME_UPDATE_FRAGMENT;
    }


    @IsAdmin
    @GetMapping(VOLUME_DELETE_PATH)
    public String deleteVolumeById(@RequestParam Long id,
                                   @RequestParam(required = false, name = "description") String descriptionQuery,
                                   @RequestParam(defaultValue = "0") Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize,
                                   Model model){

        try{
            volumeService.softDeleteById(id);
        }
        catch (NotFoundException e){
            model.addAttribute("tableError", e.getMessage());
        }

        addPageToModel(descriptionQuery, pageNumber, pageSize, model);

        return ViewNames.VOLUME_TABLE_FRAGMENT;
    }

    @IsAdmin
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
