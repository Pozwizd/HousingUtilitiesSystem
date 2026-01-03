package org.spacelab.housingutilitiessystemuser.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    
    private List<T> content;

    
    private int number;

    
    private int size;

    
    private long totalElements;

    
    private int totalPages;

    
    private boolean first;

    
    private boolean last;

    
    private int numberOfElements;

    
    private boolean hasNext;

    
    private boolean hasPrevious;

    
    public static <T> PageResponse<T> of(Page<T> page) {
        if (page == null) {
            return null;
        }

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getNumberOfElements(),
                page.hasNext(),
                page.hasPrevious());
    }
}
