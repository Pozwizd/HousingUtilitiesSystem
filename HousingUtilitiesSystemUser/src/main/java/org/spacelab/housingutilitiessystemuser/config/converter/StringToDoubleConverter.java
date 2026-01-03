package org.spacelab.housingutilitiessystemuser.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


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
