package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionType;
import com.example.product_service.entity.OptionValue;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.OptionValueRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.product_service.common.MessagePath.OPTION_VALUE_CONFLICT;
import static com.example.product_service.common.MessagePath.OPTION_VALUE_NOT_FOUND;
import static com.example.product_service.util.TestMessageUtil.getMessage;
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
    @Autowired
    EntityManager em;

    OptionType type;
    OptionValue existValue;
    OptionValue target;

    @BeforeEach
    void setFixture(){
        type = new OptionType("type");
        existValue = new OptionValue("existValue");
        target = new OptionValue("target");
        type.addOptionValue(existValue);
        type.addOptionValue(target);
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
        assertThat(response.getValueName()).isEqualTo(existValue.getOptionValue());
    }

    @Test
    @DisplayName("옵션 값 조회 테스트-실패(옵션 값 없음)")
    void getOptionValueByIdTest_integration_notFound(){
        assertThatThrownBy(() -> optionValueService.getOptionValueById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-성공")
    void updateOptionValueTest_integration_success(){
        OptionValueRequest request = new OptionValueRequest("updated");
        OptionValueResponse response = optionValueService.updateOptionValueById(existValue.getId(), request);
        em.flush(); em.clear();

        assertThat(response.getTypeId()).isEqualTo(type.getId());
        assertThat(response.getValueId()).isEqualTo(existValue.getId());
        assertThat(response.getValueName()).isEqualTo(existValue.getOptionValue());

        OptionValue optionValue = optionValueRepository.findById(existValue.getId()).get();

        assertThat(optionValue.getOptionValue()).isEqualTo("updated");
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(옵션 값 없음)")
    void updateOptionValueTest_integration_notFound(){
        OptionValueRequest request = new OptionValueRequest("updated");
        assertThatThrownBy(() -> optionValueService.updateOptionValueById(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(중복 이름)")
    void updateOptionValueTest_integration_conflict(){
        OptionValueRequest request = new OptionValueRequest("existValue");
        assertThatThrownBy(() -> optionValueService.updateOptionValueById(target.getId(), request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_VALUE_CONFLICT));
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-성공")
    void deleteOptionValueByIdTest_integration_success(){
        optionValueService.deleteOptionValueById(target.getId());
        em.flush(); em.clear();

        Optional<OptionValue> result = optionValueRepository.findById(target.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-실패(옵션 값 찾을 수 없음)")
    void deleteOptionValueByIdTest_integration_notFound(){
        assertThatThrownBy(() -> optionValueService.deleteOptionValueById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }
}