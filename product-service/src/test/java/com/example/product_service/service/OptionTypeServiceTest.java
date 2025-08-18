package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OptionTypeServiceTest {

    @Autowired
    OptionTypeService optionTypeService;
    @Autowired
    OptionTypeRepository optionTypeRepository;

    @Autowired
    MessageSourceUtil ms;

    @Autowired
    EntityManager em;

    OptionTypes existType;
    OptionValues existValue;

    @BeforeEach
    void saveFixture(){
        existType = new OptionTypes("exist");
        existValue = new OptionValues("existValueName");
        existType.addOptionValue(existValue);
        optionTypeRepository.save(existType);
    }

    @AfterEach
    void clearDB(){
        optionTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("옵션 타입 저장 테스트-성공")
    void saveOptionTypeTest_integration_success(){
        OptionTypeRequest request = new OptionTypeRequest("name");

        OptionTypeResponse response = optionTypeService.saveOptionType(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("name");
    }

    @Test
    @DisplayName("옵션 타입 저장 테스트-실패(옵션 타입 이름 중복)")
    void saveOptionTypeTest_integration_conflict(){
        OptionTypeRequest request = new OptionTypeRequest("exist");

        assertThatThrownBy(() -> optionTypeService.saveOptionType(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_TYPE_CONFLICT));
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-성공")
    void saveOptionValueTest_integration_success(){
        OptionValueRequest request = new OptionValueRequest("value");
        OptionValueResponse response = optionTypeService.saveOptionValue(existType.getId(), request);
        assertThat(response.getTypeId()).isEqualTo(existType.getId());
        assertThat(response.getValueId()).isNotNull();
        assertThat(response.getValueName()).isEqualTo("value");
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(옵션 타입 없음)")
    void saveOptionValueTest_integration_notFound(){
        OptionValueRequest request = new OptionValueRequest("valueName");

        assertThatThrownBy(() -> optionTypeService.saveOptionValue(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }
    
    @Test
    @DisplayName("옵션 값 저장 테스트-실패(옵션 값 이름 중복)")
    void saveOptionValueTest_integration_conflict(){
        OptionValueRequest request = new OptionValueRequest("existValueName");
        assertThatThrownBy(() -> optionTypeService.saveOptionValue(existType.getId(), request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_VALUE_CONFLICT));
    }

    @Test
    @DisplayName("옵션 타입 조회 테스트-성공")
    void getOptionTypesTest_integration_success() {
        OptionTypes type1 = optionTypeRepository.save(new OptionTypes("type1"));
        OptionTypes type2 = optionTypeRepository.save(new OptionTypes("type2"));
        OptionTypes type3 = optionTypeRepository.save(new OptionTypes("type3"));

        List<OptionTypeResponse> response = optionTypeService.getOptionTypes();

        em.flush(); em.clear();

        assertThat(response).hasSize(4);
        assertThat(response)
                .extracting("id", "name")
                .containsExactlyInAnyOrder(
                        tuple(type1.getId(), type1.getName()),
                        tuple(type2.getId(), type2.getName()),
                        tuple(type3.getId(), type3.getName()),
                        tuple(existType.getId(), existType.getName())
                );
    }

    @Test
    @DisplayName("옵션 값 조회 테스트-성공")
    void getOptionValuesByTypeIdTest_integration_success() {
        OptionValues value1 = new OptionValues("value1");
        OptionValues value2 = new OptionValues("value2");
        OptionValues value3 = new OptionValues("value3");
        addOptionValues(existType, value1, value2, value3);

        em.flush();
        em.clear();

        List<OptionValueResponse> response = optionTypeService.getOptionValuesByTypeId(existType.getId());
        assertThat(response).hasSize(4);
        assertThat(response)
                .extracting("valueId", "typeId", "valueName")
                .containsExactlyInAnyOrder(
                        tuple(value1.getId(), value1.getOptionType().getId(), value1.getOptionValue()),
                        tuple(value2.getId(), value2.getOptionType().getId(), value2.getOptionValue()),
                        tuple(value3.getId(), value3.getOptionType().getId(), value3.getOptionValue()),
                        tuple(existValue.getId(), existValue.getOptionType().getId(), existValue.getOptionValue())
                );
    }

    @Test
    @DisplayName("옵션 값 조회 테스트-실패(옵션 타입을 찾을 수 없음)")
    void getOptionValuesByTypeIdTest_integration_notFound(){
        assertThatThrownBy(() -> optionTypeService.getOptionValuesByTypeId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-성공")
    void updateOptionTypeTest_integration_success(){
        OptionTypeRequest request = new OptionTypeRequest("updated");

        OptionTypeResponse response = optionTypeService.updateOptionTypeById(existType.getId(), request);

        assertThat(response.getId()).isEqualTo(existType.getId());
        assertThat(response.getName()).isEqualTo("updated");
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-실패(옵션 타입을 찾을 수 없음")
    void updateOptionTypeTest_integration_notFound(){
        OptionTypeRequest request = new OptionTypeRequest("updated");

        assertThatThrownBy(() -> optionTypeService.updateOptionTypeById(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-실패(옵션 타입 이름 중복)")
    void updateOptionTypeTest_integration_conflict(){
        OptionTypes target = optionTypeRepository.save(new OptionTypes("target"));
        OptionTypeRequest request = new OptionTypeRequest("exist");

        em.flush(); em.clear();

        assertThatThrownBy(() -> optionTypeService.updateOptionTypeById(target.getId(), request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(OPTION_TYPE_CONFLICT));
    }

    @Test
    @DisplayName("옵션 타입 삭제 테스트-성공")
    void deleteOptionTypeTest_integration_success(){
        optionTypeService.deleteOptionTypeById(existType.getId());
        em.flush(); em.clear();

        Optional<OptionTypes> result = optionTypeRepository.findById(existType.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("옵션 타입 삭제 테스트-실패(옵션 타입을 찾을 수 없음)")
    void deleteOptionTypeTest_integration_notFound(){
        assertThatThrownBy(() -> optionTypeService.deleteOptionTypeById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    private void addOptionValues(OptionTypes type, OptionValues... optionValues){
        for (OptionValues optionValue : optionValues) {
            type.addOptionValue(optionValue);
        }
    }
}