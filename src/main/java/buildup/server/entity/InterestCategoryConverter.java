package buildup.server.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InterestCategoryConverter implements AttributeConverter<InterestCategory, String> {

    @Override
    public String convertToDatabaseColumn(InterestCategory attribute) {
        return attribute == null ? null : attribute.getField();
    }

    @Override
    public InterestCategory convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InterestCategory.fromField(dbData);
    }
}