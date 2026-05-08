package edu.cit.cordero.glamsched.features.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty())
            return null;
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank())
            return new ArrayList<>();
        try {
            // Handle both ["data:..."] and raw data:... formats
            String trimmed = dbData.trim();
            if (trimmed.startsWith("[")) {
                return mapper.readValue(trimmed, new TypeReference<List<String>>() {
                });
            }
            // Single raw value
            List<String> list = new ArrayList<>();
            list.add(trimmed);
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
