package com.nooul.apihelpers.springbootrest.helpers.values;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MobileConverter implements AttributeConverter<Mobile, String> {
    @Override
    public String convertToDatabaseColumn(Mobile mobile) {
        if(mobile == null) {
            return null;
        }
        return mobile.formatAsStringWithoutPlus();
    }

    @Override
    public Mobile convertToEntityAttribute(String number) {
        if(number == null) {
            return null;
        }
        return Mobile.fromString(number);
    }
}