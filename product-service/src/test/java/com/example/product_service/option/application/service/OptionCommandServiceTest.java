package com.example.product_service.option.application.service;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.option.exception.OptionErrorCode;
import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.option.application.port.OptionProductPort;
import com.example.product_service.option.application.port.OptionTypeRepository;
import com.example.product_service.option.application.service.dto.command.AddOptionValueCommand;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionValueCommand;
import com.example.product_service.option.domain.OptionType;
import com.example.product_service.option.fixture.OptionFixtureBuilder;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.product_service.option.fixture.OptionCommandFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OptionCommandServiceTest {

    @InjectMocks
    private OptionCommandService optionCommandService;

    @Mock
    private OptionTypeRepository optionTypeRepository;

    @Mock
    private OptionProductPort optionProductPort;

    @Mock
    private IdGenerator idGenerator;

    @Captor
    private ArgumentCaptor<OptionType> optionTypeCaptor;

    @Test
    @DisplayName("옵션 타입을 생성한다")
    void createOptionType() {
        //given
        CreateOptionTypeCommand command = anCreateOptionTypeCommand().build();
        Long valueId = 200L;
        Long optionTypeId = 100L;

        given(optionTypeRepository.existsByName(command.name())).willReturn(false);
        given(idGenerator.generate()).willReturn(valueId, optionTypeId);
        given(optionTypeRepository.save(any(OptionType.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        //when
        Long result = optionCommandService.createOptionType(command);
        //then
        then(optionTypeRepository).should().save(optionTypeCaptor.capture());
        OptionType savedOptionType = optionTypeCaptor.getValue();

        assertThat(result).isEqualTo(optionTypeId);
        assertThat(savedOptionType.getId()).isEqualTo(optionTypeId);
        assertThat(savedOptionType.getName()).isEqualTo(command.name());
        assertThat(savedOptionType.getOptionValues()).hasSize(1);
        assertThat(savedOptionType.getOptionValues().getFirst().getId()).isEqualTo(valueId);
        assertThat(savedOptionType.getOptionValues().getFirst().getName()).isEqualTo("BLUE");
    }

    @Test
    @DisplayName("이미 존재하는 옵션 타입 이름이면 예외가 발생하고 생성하지 않는다")
    void createOptionType_whenNameDuplicated_thenThrownException() {
        //given
        CreateOptionTypeCommand command = anCreateOptionTypeCommand().build();
        given(optionTypeRepository.existsByName(command.name())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.createOptionType(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_DUPLICATE_NAME);

        then(optionTypeRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("옵션 타입 이름을 수정한다")
    void updateOptionType() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        UpdateOptionTypeCommand command = anUpdateOptionTypeCommand().optionTypeId(optionType.getId()).build();

        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        given(optionTypeRepository.existsByNameAndIdNot(command.name(), optionType.getId())).willReturn(false);
        //when
        Long result = optionCommandService.updateOptionType(command);
        //then
        assertThat(result).isEqualTo(optionType.getId());
        assertThat(optionType.getName()).isEqualTo(command.name());
    }

    @Test
    @DisplayName("옵션 타입을 찾을 수 없으면 예외가 발생한다")
    void updateOptionType_whenNotFound_thenThrownException() {
        //given
        UpdateOptionTypeCommand command = anUpdateOptionTypeCommand().optionTypeId(999L).build();
        given(optionTypeRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.updateOptionType(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 옵션 타입과 이름이 중복되면 예외가 발생한다")
    void updateOptionType_whenNameDuplicated_thenThrownException() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        UpdateOptionTypeCommand command = anUpdateOptionTypeCommand().optionTypeId(optionType.getId()).build();

        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        given(optionTypeRepository.existsByNameAndIdNot(command.name(), optionType.getId())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.updateOptionType(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_DUPLICATE_NAME);
    }

    @Test
    @DisplayName("옵션 타입을 삭제한다")
    void deleteOptionType() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        given(optionProductPort.existsProductForOptionType(optionType.getId())).willReturn(false);
        //when
        optionCommandService.deleteOptionType(optionType.getId());
        //then
        then(optionTypeRepository).should().delete(optionType);
    }

    @Test
    @DisplayName("옵션 타입을 찾을 수 없으면 삭제시 예외가 발생한다")
    void deleteOptionType_whenNotFound_thenThrownException() {
        //given
        given(optionTypeRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.deleteOptionType(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_NOT_FOUND);

        then(optionTypeRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("옵션 타입을 사용하는 상품이 있으면 삭제시 예외가 발생한다")
    void deleteOptionType_whenInProduct_thenThrownException() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        given(optionProductPort.existsProductForOptionType(optionType.getId())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.deleteOptionType(optionType.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_IN_PRODUCT);

        then(optionTypeRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("옵션 값을 추가한다")
    void addOptionValue() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        AddOptionValueCommand command = anAddOptionValueCommand().optionTypeId(optionType.getId()).build();
        Long newValueId = 300L;

        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        given(idGenerator.generate()).willReturn(newValueId);
        //when
        Long result = optionCommandService.addOptionValue(command);
        //then
        assertThat(result).isEqualTo(newValueId);
        assertThat(optionType.getOptionValues())
                .extracting("id", "name")
                .contains(Tuple.tuple(newValueId, command.name()));
    }

    @Test
    @DisplayName("옵션 타입을 찾을 수 없으면 옵션 값 추가시 예외가 발생한다")
    void addOptionValue_whenOptionTypeNotFound_thenThrownException() {
        //given
        AddOptionValueCommand command = anAddOptionValueCommand().optionTypeId(999L).build();
        given(optionTypeRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.addOptionValue(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_NOT_FOUND);
    }

    @Test
    @DisplayName("옵션 값 이름을 수정한다")
    void updateOptionValue() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        Long optionValueId = optionType.getOptionValues().getFirst().getId();
        UpdateOptionValueCommand command = anUpdateOptionValueCommand()
                .optionTypeId(optionType.getId())
                .optionValueId(optionValueId)
                .build();

        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        //when
        Long result = optionCommandService.updateOptionValue(command);
        //then
        assertThat(result).isEqualTo(optionValueId);
        assertThat(optionType.getOptionValues().getFirst().getName()).isEqualTo(command.name());
    }

    @Test
    @DisplayName("옵션 타입을 찾을 수 없으면 옵션 값 수정시 예외가 발생한다")
    void updateOptionValue_whenOptionTypeNotFound_thenThrownException() {
        //given
        UpdateOptionValueCommand command = anUpdateOptionValueCommand().optionTypeId(999L).build();
        given(optionTypeRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.updateOptionValue(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_NOT_FOUND);
    }

    @Test
    @DisplayName("옵션 값을 삭제한다")
    void deleteOptionValue() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        Long optionValueId = optionType.getOptionValues().getFirst().getId();

        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        given(optionProductPort.existsProductForOptionValue(optionValueId)).willReturn(false);
        //when
        optionCommandService.deleteOptionValue(optionType.getId(), optionValueId);
        //then
        assertThat(optionType.getOptionValues())
                .noneMatch(value -> value.getId().equals(optionValueId));
    }

    @Test
    @DisplayName("옵션 타입을 찾을 수 없으면 옵션 값 삭제시 예외가 발생한다")
    void deleteOptionValue_whenOptionTypeNotFound_thenThrownException() {
        //given
        given(optionTypeRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.deleteOptionValue(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_TYPE_NOT_FOUND);
    }

    @Test
    @DisplayName("옵션 값이 상품에 속해있으면 삭제시 예외가 발생한다")
    void deleteOptionValue_whenInProduct_thenThrownException() {
        //given
        OptionType optionType = OptionFixtureBuilder.given().build();
        Long optionValueId = optionType.getOptionValues().getFirst().getId();

        given(optionTypeRepository.findById(optionType.getId())).willReturn(Optional.of(optionType));
        given(optionProductPort.existsProductForOptionValue(optionValueId)).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> optionCommandService.deleteOptionValue(optionType.getId(), optionValueId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OptionErrorCode.OPTION_VALUE_IN_PRODUCT);

        assertThat(optionType.getOptionValues())
                .anyMatch(value -> value.getId().equals(optionValueId));
    }
}
