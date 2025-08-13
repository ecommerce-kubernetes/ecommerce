package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
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

import static com.example.product_service.controller.util.MessagePath.OPTION_VALUE_NOT_FOUND;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OptionValueServiceUnitTest {
    @Mock
    OptionValueRepository optionValueRepository;
    @Mock
    MessageSourceUtil ms;
    @Captor
    private ArgumentCaptor<OptionValues> valuesArgumentCaptor;

    @InjectMocks
    OptionValueService optionValueService;

    @Test
    @DisplayName("옵션 값 조회 테스트-성공")
    void getOptionValueByIdTest_unit_success(){
        OptionTypes type = createOptionTypeWithSetId(1L, "type");
        OptionValues value = createOptionValueWithSetId(1L, "value");
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

    private void mockFindById(Long id, OptionValues o){
        OngoingStubbing<Optional<OptionValues>> when = when(optionValueRepository.findById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(o));
        }
    }

    private OptionTypes createOptionTypeWithSetId(Long id, String name){
        OptionTypes optionTypes = new OptionTypes(name);
        ReflectionTestUtils.setField(optionTypes, "id", id);
        return optionTypes;
    }

    private OptionValues createOptionValueWithSetId(Long id, String name){
        OptionValues optionValues = new OptionValues(name);
        ReflectionTestUtils.setField(optionValues, "id", id);
        return optionValues;
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }
}
