package com.example.product_service.option.fixture;

import com.example.product_service.option.adapter.in.web.dto.request.AddOptionValueRequest;
import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;

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

    public static AddOptionValueRequest.AddOptionValueRequestBuilder anAddOptionValueRequest() {
        return AddOptionValueRequest.builder()
                .name("RED");
    }

    public static UpdateOptionValueRequest.UpdateOptionValueRequestBuilder anUpdateOptionValueRequest() {
        return UpdateOptionValueRequest.builder()
                .name("GREEN");
    }
}
