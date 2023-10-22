package org.agard.InventoryManagement.controllers;

import jakarta.validation.ConstraintViolationException;
import org.agard.InventoryManagement.Exceptions.NotFoundException;
import org.agard.InventoryManagement.util.ViewNames;
import org.hibernate.StaleObjectStateException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomErrorController {

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

    @ExceptionHandler(NotFoundException.class)
    String handleNotFound(NotFoundException e, Model model){
        List<String> errorList = new ArrayList<>();
        errorList.add(e.getMessage());

        model.addAttribute("errorTitle", "HTTP 404 - Object not found");
        model.addAttribute("errorMessageList", errorList);
        return ViewNames.ERROR_VIEW;
    }

    @ExceptionHandler(StaleObjectStateException.class)
    String handleStaleObjectState(StaleObjectStateException e, Model model){
        List<String> errorList = new ArrayList<>();
        errorList.add(e.getMessage());

        model.addAttribute("errorTitle", "HTTP 500 - There was a problem updating this item or invalid Http Post data.");
        model.addAttribute("errorMessageList", errorList);

        return ViewNames.ERROR_VIEW;
    }
}
