package com.example.product_service.api.common.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

public class StringTrimmerDeserializer extends StdScalarDeserializer<String> {
    public StringTrimmerDeserializer(){
        super(String.class);
    }
    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String text = jsonParser.getValueAsString();
        return (text != null) ? text.trim() : null;
    }
}
