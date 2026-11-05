package com.example.product_service.option.fixture;

import com.example.product_service.option.application.service.dto.command.AddOptionValueCommand;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionValueCommand;

import java.util.List;

public class OptionCommandFixture {

    public static CreateOptionTypeCommand.CreateOptionTypeCommandBuilder anCreateOptionTypeCommand() {
        CreateOptionTypeCommand.CreateOptionValueCommand blue = CreateOptionTypeCommand.CreateOptionValueCommand.builder()
                .name("BLUE")
                .build();

        return CreateOptionTypeCommand.builder()
                .name("색상")
                .values(List.of(blue));
    }

    public static UpdateOptionTypeCommand.UpdateOptionTypeCommandBuilder anUpdateOptionTypeCommand() {
        return UpdateOptionTypeCommand.builder()
                .optionTypeId(1L)
                .name("사이즈");
    }

    public static AddOptionValueCommand.AddOptionValueCommandBuilder anAddOptionValueCommand() {
        return AddOptionValueCommand.builder()
                .optionTypeId(1L)
                .name("GREEN");
    }

    public static UpdateOptionValueCommand.UpdateOptionValueCommandBuilder anUpdateOptionValueCommand() {
        return UpdateOptionValueCommand.builder()
                .optionTypeId(1L)
                .optionValueId(10L)
                .name("YELLOW");
    }
}
