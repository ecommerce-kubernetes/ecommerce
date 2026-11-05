package com.example.product_service.option.domain;

import com.example.product_service.option.domain.context.CreateOptionTypeContext;
import com.example.product_service.option.domain.context.CreateOptionValueContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OptionValueTest {

    @Test
    @DisplayName("옵션 값을 생성한다")
    void create() {
        //given
        OptionType optionType = anOptionType();
        CreateOptionValueContext context = CreateOptionValueContext.of(10L, "BLUE");
        //when
        OptionValue optionValue = OptionValue.create(context, optionType);
        //then
        assertThat(optionValue.getId()).isEqualTo(10L);
        assertThat(optionValue.getName()).isEqualTo("BLUE");
        assertThat(optionValue.getOptionType()).isEqualTo(optionType);
    }

    @Test
    @DisplayName("옵션 값의 이름을 수정한다")
    void update() {
        //given
        OptionType optionType = anOptionType();
        OptionValue optionValue = OptionValue.create(CreateOptionValueContext.of(10L, "BLUE"), optionType);
        //when
        optionValue.update("RED");
        //then
        assertThat(optionValue.getName()).isEqualTo("RED");
    }

    @Test
    @DisplayName("옵션 값의 옵션 타입 연관관계를 해제한다")
    void detachOptionType() {
        //given
        OptionType optionType = anOptionType();
        OptionValue optionValue = OptionValue.create(CreateOptionValueContext.of(10L, "BLUE"), optionType);
        //when
        optionValue.detachOptionType();
        //then
        assertThat(optionValue.getOptionType()).isNull();
    }

    private OptionType anOptionType() {
        return OptionType.create(CreateOptionTypeContext.of(1L, "색상", List.of()));
    }
}
