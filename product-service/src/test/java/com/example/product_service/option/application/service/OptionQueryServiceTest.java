package com.example.product_service.option.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.OptionErrorCode;
import com.example.product_service.option.application.port.OptionTypeRepository;
import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.example.product_service.option.application.service.dto.result.OptionTypesResult;
import com.example.product_service.option.domain.OptionType;
import com.example.product_service.option.fixture.OptionFixtureBuilder;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OptionQueryServiceTest {

    @InjectMocks
    private OptionQueryService optionQueryService;

    @Mock
    private OptionTypeRepository optionTypeRepository;

    @Test
    @DisplayName("옵션 타입 목록을 조회한다")
    void getTypes() {
        //given
        OptionType color = OptionFixtureBuilder.given().withName("색상").withValueNames(List.of("BLUE")).build();
        OptionType size = OptionFixtureBuilder.given().withName("사이즈").withValueNames(List.of("L")).build();
        given(optionTypeRepository.findAll()).willReturn(List.of(color, size));
        //when
        OptionTypesResult result = optionQueryService.getTypes();
        //then
        assertThat(result.types()).hasSize(2)
                .extracting(OptionTypeResult::id, OptionTypeResult::name)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(color.getId(), "색상"),
                        Tuple.tuple(size.getId(), "사이즈")
                );
    }

    @Test
    @DisplayName("옵션 타입 목록이 없으면 빈 목록을 반환한다")
    void getTypes_whenEmpty_thenReturnsEmptyList() {
        //given
        given(optionTypeRepository.findAll()).willReturn(List.of());
        //when
        OptionTypesResult result = optionQueryService.getTypes();
        //then
        assertThat(result.types()).isEmpty();
    }

    @Test
    @DisplayName("옵션 타입을 조회한다")
    void getType() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        //when
        OptionTypeResult result = optionQueryService.getType(optionType.getId());
        //then
        assertThat(result.id()).isEqualTo(optionType.getId());
        assertThat(result.name()).isEqualTo(optionType.getName());
        assertThat(result.values()).hasSize(optionType.getOptionValues().size());
    }

    @Test
    @DisplayName("옵션 타입을 찾을 수 없으면 예외가 발생한다")
    void getType_whenNotFound_thenThrownException() {
        //given
        given(optionTypeRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> optionQueryService.getType(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_NOT_FOUND);
    }
}
