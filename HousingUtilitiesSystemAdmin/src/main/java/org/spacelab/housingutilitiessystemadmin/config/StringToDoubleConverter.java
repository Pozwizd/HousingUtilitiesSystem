package org.spacelab.housingutilitiessystemadmin.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Конвертер для преобразования String в Double.
 * Возвращает null для пустых или пробельных строк.
 */
@Component
public class StringToDoubleConverter implements Converter<String, Double> {
    
    @Override
    public Double convert(String source) {
        if (source.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Double.parseDouble(source.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Double format: " + source, e);
        }
    }
}
