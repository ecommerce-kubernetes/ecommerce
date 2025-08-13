package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.util.MessagePath;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.OptionValueRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.example.product_service.controller.util.MessagePath.OPTION_VALUE_NOT_FOUND;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
class OptionValueServiceTest {

    @Autowired
    OptionValueRepository optionValueRepository;
    @Autowired
    OptionTypeRepository optionTypeRepository;
    @Autowired
    MessageSourceUtil ms;
    @Autowired
    OptionValueService optionValueService;

    OptionTypes type;
    OptionValues existValue;

    @BeforeEach
    void setFixture(){
        type = new OptionTypes("type");
        existValue = new OptionValues("value");

        type.addOptionValue(existValue);
        optionTypeRepository.save(type);
    }

    @AfterEach
    void clearDB(){
        optionTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("옵션 값 조회 테스트-성공")
    void getOptionValueByIdTest_integration_success(){
        OptionValueResponse response = optionValueService.getOptionValueById(existValue.getId());

        assertThat(response.getValueId()).isEqualTo(existValue.getId());
        assertThat(response.getTypeId()).isEqualTo(type.getId());
        assertThat(response.getValueName()).isEqualTo(existValue.getValueName());
    }

    @Test
    @DisplayName("옵션 값 조회 테스트-실패(옵션 값 없음)")
    void getOptionValueByIdTest_integration_notFound(){
        assertThatThrownBy(() -> optionValueService.getOptionValueById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }
}