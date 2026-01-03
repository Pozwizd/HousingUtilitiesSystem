package org.spacelab.housingutilitiessystemadmin.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO для стабильной сериализации пагинированных данных.
 * Решает проблему нестабильной структуры JSON при сериализации PageImpl.
 *
 * @param <T> тип элементов страницы
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /** Содержимое текущей страницы */
    private List<T> content;
    
    /** Номер текущей страницы (начиная с 0) */
    private int number;
    
    /** Размер страницы */
    private int size;
    
    /** Общее количество элементов */
    private long totalElements;
    
    /** Общее количество страниц */
    private int totalPages;
    
    /** Это первая страница? */
    private boolean first;
    
    /** Это последняя страница? */
    private boolean last;
    
    /** Количество элементов на текущей странице */
    private int numberOfElements;
    
    /** Есть ли следующая страница? */
    private boolean hasNext;
    
    /** Есть ли предыдущая страница? */
    private boolean hasPrevious;
    
    /**
     * Создает PageResponse из Spring Data Page
     */
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
                page.hasPrevious()
        );
    }
}
