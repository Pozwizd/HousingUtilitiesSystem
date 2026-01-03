package org.spacelab.housingutilitiessystemadmin.config;

import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Конвертер для преобразования String в ObjectId.
 * Возвращает null для пустых или пробельных строк.
 */
@Component
public class StringToObjectIdConverter implements Converter<String, ObjectId> {
    
    @Override
    public ObjectId convert(String source) {
        if (source.trim().isEmpty()) {
            return null;
        }
        
        try {
            return new ObjectId(source);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ObjectId format: " + source, e);
        }
    }
}
