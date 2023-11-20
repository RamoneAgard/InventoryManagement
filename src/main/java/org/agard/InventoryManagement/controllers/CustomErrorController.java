package org.agard.InventoryManagement.controllers;

import jakarta.validation.ConstraintViolationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.util.ViewNames;
import org.hibernate.StaleObjectStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@ControllerAdvice
public class CustomErrorController {

    public static final String RESOURCE_DENIED_PATH = "/unauthorized";

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(TransactionSystemException.class)
    String handleJPAViolation(TransactionSystemException e, Model model) {

        if (e.getCause().getCause() instanceof ConstraintViolationException ve) {
            List errors = ve.getConstraintViolations().stream().map(cv -> {
                Map<String, String> errMap = new HashMap<>();
                errMap.put(cv.getPropertyPath().toString(), cv.getMessage());
                return errMap;
            }).collect(Collectors.toList());
            model.addAttribute("errorMessageList", errors);
        }

        model.addAttribute("errorTitle", "HTTP 500 - Server Side Error! Item constrains not met.");
        return ViewNames.ERROR_VIEW;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    String handleNotFound(NotFoundException e, Model model){
        List<String> errorList = new ArrayList<>();
        errorList.add(e.getMessage());

        model.addAttribute("errorTitle", "HTTP 404 - Object not found");
        model.addAttribute("errorMessageList", errorList);
        return ViewNames.ERROR_VIEW;
    }

    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ExceptionHandler(StaleObjectStateException.class)
    String handleStaleObjectState(StaleObjectStateException e, Model model){
        List<String> errorList = new ArrayList<>();
        String errorMsg = e.getMessage().split(":")[0];
        errorList.add(errorMsg);

        model.addAttribute("errorTitle", "HTTP 412 - There was a problem updating this " +
                "item due to stale object state (invalid Http Post data)");
        model.addAttribute("errorMessageList", errorList);

        return ViewNames.ERROR_VIEW;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    String handleAccessDenied(AccessDeniedException e, Model model){
        List<String> errorList = new ArrayList<>();
        errorList.add(e.getMessage());

        model.addAttribute("errorTitle", "HTTP 403 - User does not have access");
        model.addAttribute("errorMessageList", errorList);

        return ViewNames.ERROR_VIEW;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @GetMapping(RESOURCE_DENIED_PATH)
    public String resourceAccessDenied(Model model){

        List<String> errorList = new ArrayList<>();
        errorList.add("This User Role does not have access to this page");

        model.addAttribute("errorTitle", "HTTP 403 - User does not have access");
        model.addAttribute("errorMessageList", errorList);

        return ViewNames.ERROR_VIEW;
    }


}
