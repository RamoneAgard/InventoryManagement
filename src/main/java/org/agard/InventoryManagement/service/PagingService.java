package org.agard.InventoryManagement.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public interface PagingService {

    Integer DEFAULT_PAGE_SIZE = 20;

    Integer MAX_PAGE_SIZE = 50;

    default PageRequest buildPageRequest(Integer pageNumber, Integer pageSize, Sort sort){
        if(pageNumber == null || pageNumber < 0){
            pageNumber = 0;
        }

        if(pageSize == null || (pageSize < 1 || pageSize > MAX_PAGE_SIZE)){
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
