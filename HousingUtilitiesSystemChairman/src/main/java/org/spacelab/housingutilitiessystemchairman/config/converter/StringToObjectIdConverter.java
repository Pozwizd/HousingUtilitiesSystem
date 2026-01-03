package org.spacelab.housingutilitiessystemchairman.config.converter;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
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
