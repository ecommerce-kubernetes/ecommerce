package com.example.product_service.option.fixture;

import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;

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

    public static UpdateOptionTypeRequest.UpdateOptionTypeRequestBuilder anUpdateOptionTypeRequest() {
        return UpdateOptionTypeRequest.builder()
                .name("사이즈");
    }
}
