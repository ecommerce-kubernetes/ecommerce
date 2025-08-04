package com.example.product_service.service;

import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.OptionValuesUpdateRequestDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypesRepository;
import com.example.product_service.repository.OptionValuesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class OptionValueServiceImplTest {
    @Autowired
    OptionValueService optionValueService;
    @Autowired
    OptionValuesRepository optionValuesRepository;
    @Autowired
    OptionTypesRepository optionTypesRepository;

    OptionTypes optionTypes;
    @BeforeEach
    void initOptionTypes(){
        optionTypes = optionTypesRepository.save(new OptionTypes("사이즈"));
    }

    @AfterEach
    void clearOptionTypes(){
        optionTypesRepository.deleteAll();
    }

    @Test
    @DisplayName("OptionValues 저장 테스트")
    @Transactional
    void saveOptionValuesTest(){
        String value = "XL";
        //request
        OptionValueRequest requestDto = new OptionValueRequest(optionTypes.getId(), value);

        //Test
        OptionValuesResponseDto responseDto = optionValueService.saveOptionValues(requestDto);

        //검증
        assertThat(responseDto.getOptionTypeId()).isEqualTo(optionTypes.getId());
        assertThat(responseDto.getOptionValue()).isEqualTo(value);

        Optional<OptionValues> savedOptionValue = optionValuesRepository.findById(responseDto.getId());

        assertThat(savedOptionValue).isPresent();
    }

    @Test
    @DisplayName("OptionValues 배치 삭제 테스트")
    @Transactional
    void batchDeleteOptionValueTest(){

        //초기 데이터
        OptionValues xl = optionValuesRepository.save(new OptionValues("XL", optionTypes));
        OptionValues l = optionValuesRepository.save(new OptionValues("L", optionTypes));
        OptionValues m = optionValuesRepository.save(new OptionValues("M", optionTypes));
        IdsRequestDto requestDto =
                new IdsRequestDto(List.of(xl.getId(), l.getId()));

        optionValueService.batchDeleteOptionValues(requestDto);
        List<OptionValues> result = optionValuesRepository.findByIdIn(List.of(xl.getId(), l.getId()));

        assertThat(result).isEmpty();
        Optional<OptionValues> optionalM = optionValuesRepository.findById(m.getId());

        assertThat(optionalM).isPresent();
    }

    @Test
    @DisplayName("OptionValues 배치 삭제 테스트 _ 존재하지 않는 id 인 경우")
    @Transactional
    void batchDeleteOptionValueTest_NotFoundIds(){
        OptionValues xl = optionValuesRepository.save(new OptionValues("XL", optionTypes));
        OptionValues l = optionValuesRepository.save(new OptionValues("L", optionTypes));

        IdsRequestDto requestDto =
                new IdsRequestDto(List.of(xl.getId(), l.getId(), 999L));

        assertThatThrownBy(() -> optionValueService.batchDeleteOptionValues(requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found OptionValue ids : [999]");
    }

    @Test
    @DisplayName("OptionValues 변경 테스트")
    @Transactional
    void modifyOptionValuesTest(){
        String modifyOptionValue="L";
        OptionValues xl = optionValuesRepository.save(new OptionValues("XL", optionTypes));
        OptionValuesUpdateRequestDto requestDto = new OptionValuesUpdateRequestDto(modifyOptionValue);
        OptionValuesResponseDto responseDto = optionValueService.modifyOptionValues(xl.getId(), requestDto);

        assertThat(responseDto.getOptionValue()).isEqualTo(modifyOptionValue);
        assertThat(responseDto.getId()).isEqualTo(xl.getId());
        assertThat(responseDto.getOptionTypeId()).isEqualTo(optionTypes.getId());
    }

    @Test
    @DisplayName("OptionValues 변경 테스트_NotFound")
    @Transactional
    void modifyOptionValuesTesT_NotFound(){
        OptionValuesUpdateRequestDto requestDto = new OptionValuesUpdateRequestDto("L");
        assertThatThrownBy(()-> optionValueService.modifyOptionValues(999L, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found OptionValue");
    }
}