package com.example.product_service.option.domain;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.OptionErrorCode;
import com.example.product_service.option.domain.context.CreateOptionTypeContext;
import com.example.product_service.option.domain.context.CreateOptionValueContext;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTypeTest {

    @Test
    @DisplayName("옵션 값들과 함께 옵션 타입을 생성한다")
    void create() {
        //given
        CreateOptionTypeContext context = CreateOptionTypeContext.of(
                1L,
                "색상",
                List.of(
                        CreateOptionValueContext.of(2L, "BLUE"),
                        CreateOptionValueContext.of(3L, "RED")
                )
        );
        //when
        OptionType optionType = OptionType.create(context);
        //then
        assertThat(optionType.getId()).isEqualTo(1L);
        assertThat(optionType.getName()).isEqualTo("색상");
        assertThat(optionType.getOptionValues()).hasSize(2)
                .extracting(OptionValue::getId, OptionValue::getName)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(2L, "BLUE"),
                        Tuple.tuple(3L, "RED")
                );
        assertThat(optionType.getOptionValues())
                .allMatch(value -> value.getOptionType() == optionType);
    }

    @Test
    @DisplayName("옵션 값 이름이 중복되면 예외가 발생한다")
    void create_whenDuplicateValueNames_thenThrownException() {
        //given
        CreateOptionTypeContext context = CreateOptionTypeContext.of(
                1L,
                "색상",
                List.of(
                        CreateOptionValueContext.of(2L, "BLUE"),
                        CreateOptionValueContext.of(3L, "BLUE")
                )
        );
        //when
        //then
        assertThatThrownBy(() -> OptionType.create(context))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_VALUE_DUPLICATE_NAME);
    }

    @Test
    @DisplayName("옵션 타입의 이름을 수정한다")
    void update() {
        //given
        OptionType optionType = anOptionType();
        //when
        optionType.update("사이즈");
        //then
        assertThat(optionType.getName()).isEqualTo("사이즈");
    }

    @Test
    @DisplayName("옵션 값을 추가한다")
    void addOptionValue() {
        //given
        OptionType optionType = anOptionType();
        CreateOptionValueContext context = CreateOptionValueContext.of(10L, "GREEN");
        //when
        OptionValue addedValue = optionType.addOptionValue(context);
        //then
        assertThat(addedValue.getId()).isEqualTo(10L);
        assertThat(addedValue.getName()).isEqualTo("GREEN");
        assertThat(addedValue.getOptionType()).isEqualTo(optionType);
        assertThat(optionType.getOptionValues()).hasSize(3).contains(addedValue);
    }

    @Test
    @DisplayName("이미 존재하는 이름으로 옵션 값을 추가하면 예외가 발생한다")
    void addOptionValue_whenDuplicateName_thenThrownException() {
        //given
        OptionType optionType = anOptionType();
        CreateOptionValueContext context = CreateOptionValueContext.of(10L, "BLUE");
        //when
        //then
        assertThatThrownBy(() -> optionType.addOptionValue(context))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_VALUE_DUPLICATE_NAME);

        assertThat(optionType.getOptionValues()).hasSize(2);
    }

    @Test
    @DisplayName("옵션 값의 이름을 수정한다")
    void updateOptionValue() {
        //given
        OptionType optionType = anOptionType();
        Long optionValueId = optionType.getOptionValues().getFirst().getId();
        //when
        optionType.updateOptionValue(optionValueId, "GREEN");
        //then
        assertThat(optionType.getOptionValues().getFirst().getName()).isEqualTo("GREEN");
    }

    @Test
    @DisplayName("자기 자신과 동일한 이름으로 수정해도 예외가 발생하지 않는다")
    void updateOptionValue_whenRenamingToSameName_thenNoException() {
        //given
        OptionType optionType = anOptionType();
        OptionValue value = optionType.getOptionValues().getFirst();
        //when
        //then
        assertThatCode(() -> optionType.updateOptionValue(value.getId(), value.getName()))
                .doesNotThrowAnyException();
        assertThat(value.getName()).isEqualTo("BLUE");
    }

    @Test
    @DisplayName("존재하지 않는 옵션 값을 수정하면 예외가 발생한다")
    void updateOptionValue_whenValueNotFound_thenThrownException() {
        //given
        OptionType optionType = anOptionType();
        //when
        //then
        assertThatThrownBy(() -> optionType.updateOptionValue(999L, "GREEN"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_VALUE_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 옵션 값과 동일한 이름으로 수정하면 예외가 발생한다")
    void updateOptionValue_whenDuplicateNameWithAnotherValue_thenThrownException() {
        //given
        OptionType optionType = anOptionType();
        Long optionValueId = optionType.getOptionValues().getFirst().getId();
        //when
        //then
        assertThatThrownBy(() -> optionType.updateOptionValue(optionValueId, "RED"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_VALUE_DUPLICATE_NAME);
    }

    @Test
    @DisplayName("옵션 값을 삭제한다")
    void deleteOptionValue() {
        //given
        OptionType optionType = anOptionType();
        Long optionValueId = optionType.getOptionValues().getFirst().getId();
        //when
        optionType.deleteOptionValue(optionValueId);
        //then
        assertThat(optionType.getOptionValues()).hasSize(1)
                .noneMatch(value -> value.getId().equals(optionValueId));
    }

    @Test
    @DisplayName("존재하지 않는 옵션 값을 삭제하면 예외가 발생한다")
    void deleteOptionValue_whenValueNotFound_thenThrownException() {
        //given
        OptionType optionType = anOptionType();
        //when
        //then
        assertThatThrownBy(() -> optionType.deleteOptionValue(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_VALUE_NOT_FOUND);
    }

    private OptionType anOptionType() {
        CreateOptionTypeContext context = CreateOptionTypeContext.of(
                1L,
                "색상",
                List.of(
                        CreateOptionValueContext.of(2L, "BLUE"),
                        CreateOptionValueContext.of(3L, "RED")
                )
        );
        return OptionType.create(context);
    }
}
