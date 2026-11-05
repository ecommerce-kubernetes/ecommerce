package com.example.product_service.option.fixture;

import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.option.domain.OptionType;
import com.example.product_service.option.domain.context.CreateOptionTypeContext;
import com.example.product_service.option.domain.context.CreateOptionValueContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class OptionFixtureBuilder {

    private static final AtomicLong idSeq = new AtomicLong(100L);
    private static final IdGenerator ID_GENERATOR = idSeq::getAndIncrement;

    private String name = "색상";
    private List<String> valueNames = List.of("BLUE", "RED");

    public static OptionFixtureBuilder given() {
        return new OptionFixtureBuilder();
    }

    public OptionFixtureBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public OptionFixtureBuilder withValueNames(List<String> valueNames) {
        this.valueNames = valueNames;
        return this;
    }

    public OptionType build() {
        List<CreateOptionValueContext> valueContexts = valueNames.stream()
                .map(valueName -> CreateOptionValueContext.of(ID_GENERATOR.generate(), valueName))
                .toList();

        CreateOptionTypeContext context = CreateOptionTypeContext.of(ID_GENERATOR.generate(), name, valueContexts);
        return OptionType.create(context);
    }
}
