package com.example.product_service.option.fixture;

import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionTypeRequest;

import java.util.List;

public class OptionRequestFixture {

    public static CreateOptionTypeRequest.CreateOptionTypeRequestBuilder anCreateOptionTypeRequest() {
        CreateOptionTypeRequest.CreateOptionValueRequest blue = CreateOptionTypeRequest.CreateOptionValueRequest.builder()
                .name("BLUE")
                .build();

        return CreateOptionTypeRequest.builder()
                .name("색상")
                .values(List.of(blue));
    }
}
