package org.agard.InventoryManagement.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public interface PagingService {

    Integer DEFAULT_PAGE_SIZE = 20;

    Integer MAX_PAGE_SIZE = 50;


    /**
     * @param pageNumber page number of the returned PageRequest object starting at 0, null defaults to 0
     * @param pageSize page size of the returned PageRequest object starting 1.
     *                0, null, or values over MAX_PAGE_SIZE default to DEFAULT_PAGE_SIZE
     * @param sort sort object to apply to the returned PageRequestObject
     * @return PageRequest object with the supplied params
     */
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
