package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.util.MessagePath;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.service.OptionTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OptionTypeServiceUnitTest {
    @Mock
    OptionTypeRepository optionTypeRepository;
    @Mock
    MessageSourceUtil ms;
    @Captor
    private ArgumentCaptor<OptionTypes> captor;
    @InjectMocks
    OptionTypeService optionTypeService;

    @Test
    @DisplayName("옵션 타입 생성 테스트-성공")
    void saveOptionTypeTest_unit_success(){
        OptionTypeRequest request = new OptionTypeRequest("name");
        mockExistsName("name", false);
        when(optionTypeRepository.save(any(OptionTypes.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OptionTypeResponse response = optionTypeService.saveOptionType(request);

        verify(optionTypeRepository).save(captor.capture());

        OptionTypes value = captor.getValue();
        assertThat(value.getName()).isEqualTo("name");

        assertThat(response.getName()).isEqualTo("name");
    }

    @Test
    @DisplayName("옵션 타입 생성 테스트-실패(이름 중복)")
    void saveOptionTypeTest_unit_conflict(){
        OptionTypeRequest request = new OptionTypeRequest("duplicate");
        mockExistsName("duplicate", true);
        mockMessageUtil(OPTION_TYPE_CONFLICT, "OptionType already exists");
        assertThatThrownBy(() -> optionTypeService.saveOptionType(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_TYPE_CONFLICT));
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-성공")
    void saveOptionValueTest_unit_success(){
        OptionValueRequest request = new OptionValueRequest("name");
        OptionTypes optionType = spy(createOptionTypeWithSetId(1L, "optionType"));
        mockFindById(1L, optionType);
        OptionValueResponse response = optionTypeService.saveOptionValue(1L, request);
        assertThat(response.getTypeId()).isEqualTo(1L);
        assertThat(response.getValue()).isEqualTo("name");

        verify(optionType).addOptionValue(any(OptionValues.class));
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(옵션 타입 없음)")
    void saveOptionValueTest_unit_notFound(){
        OptionValueRequest request = new OptionValueRequest("name");
        mockMessageUtil(OPTION_TYPE_NOT_FOUND, "OptionType not found");
        mockFindById(1L, null);
        assertThatThrownBy(() -> optionTypeService.saveOptionValue(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(옵션 값 이름 중복)")
    void saveOptionValueTest_unit_conflict(){
        OptionValueRequest request = new OptionValueRequest("duplicate");
        mockMessageUtil(OPTION_VALUE_CONFLICT, "OptionValue already exists");
        OptionTypes real = createOptionTypeWithSetId(1L, "optionType");
        OptionValues duplicate = new OptionValues("duplicate");
        real.addOptionValue(duplicate);

        OptionTypes optionType = spy(real);
        mockFindById(1L, optionType);

        assertThatThrownBy(() -> optionTypeService.saveOptionValue(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_VALUE_CONFLICT));

        verify(optionType, never()).addOptionValue(any(OptionValues.class));
    }

    private void mockExistsName(String name, boolean isExists){
        OngoingStubbing<Boolean> when = when(optionTypeRepository.existsByName(name));
        if(isExists){
            when.thenReturn(true);
        } else {
            when.thenReturn(false);
        }
    }

    private void mockFindById(Long id, OptionTypes o){
        OngoingStubbing<Optional<OptionTypes>> when = when(optionTypeRepository.findById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(o));
        }
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }

    private OptionTypes createOptionTypeWithSetId(Long id, String name){
        OptionTypes optionTypes = new OptionTypes(name);
        ReflectionTestUtils.setField(optionTypes, "id", id);
        return optionTypes;
    }
}
