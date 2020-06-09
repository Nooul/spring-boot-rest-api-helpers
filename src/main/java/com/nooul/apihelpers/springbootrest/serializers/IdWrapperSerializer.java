package com.nooul.apihelpers.springbootrest.serializers;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class IdWrapperSerializer extends StdSerializer<Integer> {

    public IdWrapperSerializer() {
        super(Integer.class);
    }

    public IdWrapperSerializer(Class<Integer> t) {
        super(t);
    }

    @Override
    public void serialize(Integer swe,
                          JsonGenerator jgen,
                          SerializerProvider sp) throws IOException, JsonGenerationException {

        jgen.writeStartObject();
        jgen.writeNumberField("id", swe);
        jgen.writeEndObject();
    }
}