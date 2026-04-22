package com.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class PagedResponse<T> {
    public List<T> content;
    public int page;
    public int size;
    public long totalElements;
    public int totalPages;
    public boolean last;
}
