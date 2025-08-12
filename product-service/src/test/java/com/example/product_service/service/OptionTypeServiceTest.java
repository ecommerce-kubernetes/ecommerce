package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.repository.OptionTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

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
        existType = optionTypeRepository.save(new OptionTypes("exist"));
    }

    @AfterEach
    void clearDB(){
        optionTypeRepository.deleteAll();
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
}