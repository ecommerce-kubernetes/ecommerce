package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.util.MessagePath;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OptionTypeServiceTest {

    @Autowired
    OptionTypeService optionTypeService;
    @Autowired
    OptionTypeRepository optionTypeRepository;

    @Autowired
    MessageSourceUtil ms;

    OptionTypes existType;

    @BeforeEach
    void saveFixture(){
        existType = new OptionTypes("exist");
        existType.addOptionValue(new OptionValues("duplicate"));
        optionTypeRepository.save(existType);
    }

    @AfterEach
    void clearDB(){
        optionTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("옵션 타입 저장 테스트-성공")
    @Transactional
    void saveOptionTypeTest_integration_success(){
        OptionTypeRequest request = new OptionTypeRequest("name");

        OptionTypeResponse response = optionTypeService.saveOptionType(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("name");
    }

    @Test
    @DisplayName("옵션 타입 저장 테스트-실패(옵션 타입 이름 중복)")
    @Transactional
    void saveOptionTypeTest_integration_conflict(){
        OptionTypeRequest request = new OptionTypeRequest("exist");

        assertThatThrownBy(() -> optionTypeService.saveOptionType(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_TYPE_CONFLICT));
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-성공")
    @Transactional
    void saveOptionValueTest_integration_success(){
        OptionValueRequest request = new OptionValueRequest("value");
        OptionValueResponse response = optionTypeService.saveOptionValue(existType.getId(), request);
        assertThat(response.getTypeId()).isEqualTo(existType.getId());
        assertThat(response.getValueId()).isNotNull();
        assertThat(response.getValueName()).isEqualTo("value");
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(옵션 타입 없음)")
    @Transactional
    void saveOptionValueTest_integration_notFound(){
        OptionValueRequest request = new OptionValueRequest("valueName");

        assertThatThrownBy(() -> optionTypeService.saveOptionValue(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }
    
    @Test
    @DisplayName("옵션 값 저장 테스트-실패(옵션 값 이름 중복)")
    @Transactional
    void saveOptionValueTest_integration_conflict(){
        OptionValueRequest request = new OptionValueRequest("duplicate");
        assertThatThrownBy(() -> optionTypeService.saveOptionValue(existType.getId(), request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_VALUE_CONFLICT));
    }
}