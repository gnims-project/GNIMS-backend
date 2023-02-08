package com.gnims.project.domain.friendship.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Getter
public class PageDto {
    private int totalPages;
    private long totalElements;
    private Pageable pageable;

    public PageDto(Page page) {
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.pageable = page.getPageable();
    }
}
