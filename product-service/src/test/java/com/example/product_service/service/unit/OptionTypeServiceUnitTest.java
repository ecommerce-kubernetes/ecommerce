package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.util.MessagePath;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.exception.DuplicateResourceException;
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

import static com.example.product_service.controller.util.MessagePath.OPTION_TYPE_CONFLICT;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private void mockExistsName(String name, boolean isExists){
        OngoingStubbing<Boolean> when = when(optionTypeRepository.existsByName(name));
        if(isExists){
            when.thenReturn(true);
        } else {
            when.thenReturn(false);
        }
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }
}
