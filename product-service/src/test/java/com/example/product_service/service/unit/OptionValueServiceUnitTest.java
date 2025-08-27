package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionType;
import com.example.product_service.entity.OptionValue;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionValueRepository;
import com.example.product_service.service.OptionValueService;
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

import static com.example.product_service.common.MessagePath.OPTION_VALUE_CONFLICT;
import static com.example.product_service.common.MessagePath.OPTION_VALUE_NOT_FOUND;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OptionValueServiceUnitTest {
    @Mock
    OptionValueRepository optionValueRepository;
    @Mock
    MessageSourceUtil ms;
    @Captor
    private ArgumentCaptor<OptionValue> valuesArgumentCaptor;

    @InjectMocks
    OptionValueService optionValueService;

    @Test
    @DisplayName("옵션 값 조회 테스트-성공")
    void getOptionValueByIdTest_unit_success(){
        OptionType type = createOptionTypeWithSetId(1L, "type");
        OptionValue value = createOptionValueWithSetId(1L, "value");
        type.addOptionValue(value);
        mockFindById(1L, value);
        OptionValueResponse response = optionValueService.getOptionValueById(1L);

        assertThat(response.getValueId()).isEqualTo(1L);
        assertThat(response.getTypeId()).isEqualTo(1L);
        assertThat(response.getValueName()).isEqualTo("value");
    }

    @Test
    @DisplayName("옵션 값 조회 테스트-실패(없음)")
    void getOptionValueByIdTest_unit_notFound(){
        mockMessageUtil(OPTION_VALUE_NOT_FOUND, "OptionValue not found");
        mockFindById(1L, null);

        assertThatThrownBy(() -> optionValueService.getOptionValueById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-성공")
    void updateOptionValueTest_unit_success(){
        OptionType type = createOptionTypeWithSetId(1L, "type");
        OptionValue value = createOptionValueWithSetId(1L, "duplicated");
        type.addOptionValue(value);
        mockFindById(1L, value);
        OptionValueRequest request = new OptionValueRequest("updated");

        OptionValueResponse response = optionValueService.updateOptionValueById(1L, request);

        assertThat(response.getValueId()).isEqualTo(1L);
        assertThat(response.getValueName()).isEqualTo("updated");
        assertThat(response.getTypeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(옵션 값 없음)")
    void updateOptionValueTest_unit_notFound(){
        OptionValueRequest request = new OptionValueRequest("updated");
        mockFindById(1L, null);
        mockMessageUtil(OPTION_VALUE_NOT_FOUND,"OptionValue not found");
        assertThatThrownBy(() -> optionValueService.updateOptionValueById(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(옵션 값 이름 중복)")
    void updateOptionValueTest_unit_conflict(){
        OptionType type = createOptionTypeWithSetId(1L, "type");
        OptionValue value = createOptionValueWithSetId(1L, "duplicated");
        type.addOptionValue(value);
        mockMessageUtil(OPTION_VALUE_CONFLICT, "OptionValue already exists");
        mockFindById(1L, value);
        OptionValueRequest request = new OptionValueRequest("duplicated");

        assertThatThrownBy(() -> optionValueService.updateOptionValueById(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_VALUE_CONFLICT));
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-성공")
    void deleteOptionValueByIdTest_unit_success(){
        OptionType type = spy(createOptionTypeWithSetId(1L, "type"));
        OptionValue value = spy(createOptionValueWithSetId(1L, "value"));
        type.addOptionValue(value);
        mockFindById(1L, value);

        optionValueService.deleteOptionValueById(1L);

        verify(type).removeOptionValue(value);
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-실패(옵션 값 찾을 수 없음)")
    void deleteOptionValueByIdTest_unit_notFound(){
        mockFindById(1L, null);
        mockMessageUtil(OPTION_VALUE_NOT_FOUND, "OptionValue not found");

        assertThatThrownBy(() -> optionValueService.deleteOptionValueById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }

    private void mockFindById(Long id, OptionValue o){
        OngoingStubbing<Optional<OptionValue>> when = when(optionValueRepository.findById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(o));
        }
    }

    private OptionType createOptionTypeWithSetId(Long id, String name){
        OptionType optionType = new OptionType(name);
        ReflectionTestUtils.setField(optionType, "id", id);
        return optionType;
    }

    private OptionValue createOptionValueWithSetId(Long id, String name){
        OptionValue optionValue = new OptionValue(name);
        ReflectionTestUtils.setField(optionValue, "id", id);
        return optionValue;
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }
}
