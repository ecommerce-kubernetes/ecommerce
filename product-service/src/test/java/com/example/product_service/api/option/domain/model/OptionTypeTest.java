package com.example.product_service.api.option.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionTypeTest {

    @Test
    @DisplayName("옵션을 생성한다")
    void create(){
        //given
        //when
        OptionType optionType = OptionType.create("사이즈", List.of("XL", "L"));
        //then
        assertThat(optionType.getName()).isEqualTo("사이즈");
        assertThat(optionType.getOptionValues())
                .hasSize(2)
                .extracting(OptionValue::getName)
                .containsExactly("XL", "L");
    }

    @Test
    @DisplayName("옵션을 변경한다")
    void update(){
        //given
        OptionType optionType = OptionType.create("사이즈", List.of("XL", "L"));
        //when
        optionType.update("용량", List.of("256GB", "128GB", "64GB"));
        //then
        assertThat(optionType.getName()).isEqualTo("용량");
        assertThat(optionType.getOptionValues()).hasSize(3)
                .extracting(OptionValue::getName)
                .containsExactly("256GB", "128GB", "64GB");
    }
}
