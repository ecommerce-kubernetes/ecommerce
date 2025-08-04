package com.example.product_service.service;

import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypesResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionTypesRepository;
import com.example.product_service.repository.OptionValuesRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Slf4j
class OptionTypeServiceImplTest {

    @Autowired
    OptionTypeService optionTypeService;
    @Autowired
    OptionTypesRepository optionTypesRepository;
    @Autowired
    OptionValuesRepository optionValuesRepository;

    @Test
    @DisplayName("OptionTypes 저장 테스트")
    @Transactional
    void OptionTypesSaveTest(){
        // OptionTypes request
        OptionTypeRequest optionTypeRequest = new OptionTypeRequest("사이즈");

        // optionTypeService
        OptionTypesResponseDto responseDto = optionTypeService.saveOptionTypes(optionTypeRequest);

        //검증
        assertThat(responseDto.getName()).isEqualTo(optionTypeRequest.getName());

        OptionTypes optionTypes = optionTypesRepository.findById(responseDto.getId()).get();
        assertThat(optionTypes).isNotNull();
    }

    @Test
    @DisplayName("OptionTypes 저장 테스트_중복 이름")
    @Transactional
    void OptionTypesSaveTest_DuplicateName() {
        /*
            name : unique 이므로 중복된 이름 저장시 DuplicateResourceException 발생
         */
        String duplicateName = "중복된 이름";
        //중복 이름 저장
        optionTypesRepository.save(new OptionTypes(duplicateName));

        OptionTypeRequest requestDto = new OptionTypeRequest(duplicateName);
        assertThatThrownBy(() -> optionTypeService.saveOptionTypes(requestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("OptionTypes name Conflict");
    }

    @ParameterizedTest
    @DisplayName("OptionTypes 조회 테스트")
    @MethodSource("provideGetOptionTypeRequest")
    @Transactional
    void getOptionTypesTest(List<OptionTypes> saveEntity, Pageable pageable, String query, int totalElement, String[] names){
        optionTypesRepository.saveAll(saveEntity);

        PageDto<OptionTypesResponseDto> optionTypesResponse = optionTypeService.getOptionTypes(query,pageable);

        assertThat(optionTypesResponse.getPageSize()).isEqualTo(pageable.getPageSize());
        assertThat(optionTypesResponse.getCurrentPage()).isEqualTo(pageable.getPageNumber());
        assertThat(optionTypesResponse.getTotalElement()).isEqualTo(totalElement);

        List<OptionTypesResponseDto> content = optionTypesResponse.getContent();

        assertThat(content)
                .extracting(OptionTypesResponseDto::getName)
                .containsExactlyInAnyOrder(names);
    }

    @Test
    @DisplayName("OptionTypes 변경 테스트")
    @Transactional
    void modifyOptionTypesTest(){
        // target 저장
        OptionTypes saved = optionTypesRepository.save(new OptionTypes("사이즈"));
        // 변경 Request
        OptionTypeRequest requestDto = new OptionTypeRequest("용량");

        //Test
        OptionTypesResponseDto responseDto = optionTypeService.modifyOptionTypes(saved.getId(), requestDto);

        assertThat(responseDto.getId()).isEqualTo(saved.getId());
        assertThat(responseDto.getName()).isEqualTo(requestDto.getName());


        OptionTypes modifiedOptionType = optionTypesRepository.findById(saved.getId()).get();

        assertThat(modifiedOptionType.getName()).isEqualTo(requestDto.getName());
    }

    @Test
    @DisplayName("OptionTypes 변경 테스트_중복이름")
    @Transactional
    void modifyOptionTypesTest_DuplicateName(){
        //이미 있는 옵션 이름
        optionTypesRepository.save(new OptionTypes("중복 이름"));

        //변경할 타깃 옵션
        OptionTypes saved = optionTypesRepository.save(new OptionTypes("타깃"));
        //변경 request
        OptionTypeRequest requestDto = new OptionTypeRequest("중복 이름");

        //검증
        assertThatThrownBy(()-> optionTypeService.modifyOptionTypes(saved.getId(), requestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("OptionTypes name Conflict");
    }

    @Test
    @DisplayName("OptionTypes 변경 테스트_OptionTypes 찾을 수 없음")
    @Transactional
    void modifyOptionTypesTest_NotFound(){
        OptionTypeRequest requestDto = new OptionTypeRequest("사이즈");
        // 없는 Id를 변경
        assertThatThrownBy(() -> optionTypeService.modifyOptionTypes(999L, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found OptionType");
    }

    @Test
    @DisplayName("OptionTypes 삭제 테스트")
    @Transactional
    void deleteOptionTypesTest(){

        //초기 데이터
        OptionTypes type = new OptionTypes("사이즈");
        OptionValues value = new OptionValues("XL", type);
        type.addOptionValue(value);
        OptionTypes merged = optionTypesRepository.save(type);
        Long typeId = merged.getId();
        Long valueId = merged.getOptionValues().get(0).getId();

        //Test
        optionTypeService.deleteOptionTypes(typeId);

        //검증
        assertThat(optionTypesRepository.findById(typeId)).isEmpty();
        assertThat(optionValuesRepository.findById(valueId)).isEmpty();
    }

    @Test
    @DisplayName("OptionTypes 삭제 테스트 _ OptionTypes 를 찾을 수 없음")
    @Transactional
    void deleteOptionTypesTest_NotFoundOptionTypes(){

        //없는 데이터 삭제 요청
        assertThatThrownBy(() -> optionTypeService.deleteOptionTypes(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found OptionType");
    }

    @Test
    @DisplayName("OptionTypes 배치 삭제 테스트")
    @Transactional
    void batchDeleteOptionTypesTest(){

        //초기 데이터
        OptionTypes saved1 = optionTypesRepository.save(new OptionTypes("테스트 옵션1"));
        OptionTypes saved2 = optionTypesRepository.save(new OptionTypes("테스트 옵션2"));
        OptionTypes saved3 = optionTypesRepository.save(new OptionTypes("테스트 옵션3"));

        // 요청 request
        IdsRequestDto requestDto =
                new IdsRequestDto(List.of(saved1.getId(), saved2.getId()));


        //Test
        optionTypeService.batchDeleteOptionTypes(requestDto);


        // 검증
        List<OptionTypes> result = optionTypesRepository.findByIdIn(List.of(saved1.getId(), saved2.getId()));

        assertThat(result).isEmpty();
        Optional<OptionTypes> existOptionType = optionTypesRepository.findById(saved3.getId());

        assertThat(existOptionType).isPresent();
    }

    @Test
    @DisplayName("OptionTypes 배치 삭제 테스트 _ 존재하지 않는 id 인 경우")
    @Transactional
    void batchDeleteOptionTypesTest_NotFoundIds(){

        //초기 데이터
        OptionTypes saved1 = optionTypesRepository.save(new OptionTypes("테스트 옵션1"));
        OptionTypes saved2 = optionTypesRepository.save(new OptionTypes("테스트 옵션2"));

        // request 없는 id 포함
        IdsRequestDto requestDto =
                new IdsRequestDto(List.of(saved1.getId(), saved2.getId(), 999L));

        // 검증
        assertThatThrownBy(() -> optionTypeService.batchDeleteOptionTypes(requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found OptionType ids : [999]");
    }

    @Test
    @DisplayName("OptionValues 조회")
    @Transactional
    void getOptionValuesByTypeId(){
        //초기데이터
        OptionTypes optionTypes = new OptionTypes("사이즈");
        OptionValues optionValues1 = new OptionValues("XL", optionTypes);
        OptionValues optionValues2 = new OptionValues("L", optionTypes);
        optionTypes.addOptionValue(optionValues1);
        optionTypes.addOptionValue(optionValues2);

        OptionTypes saved = optionTypesRepository.save(optionTypes);

        List<OptionValuesResponseDto> result = optionTypeService.getOptionValuesByTypeId(saved.getId());

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(OptionValuesResponseDto::getOptionValue)
                .containsExactlyInAnyOrder("XL", "L");

        assertThat(result)
                .allSatisfy(dto -> assertThat(dto.getOptionTypeId()).isEqualTo(saved.getId()));
    }

    @Test
    @DisplayName("OptionValues 조회_OptionType 을 찾을 수 없음")
    @Transactional
    void getOptionValuesByTypeId_NotFoundOptionTypes(){
        assertThatThrownBy(() -> optionTypeService.getOptionValuesByTypeId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found OptionTypes");
    }

    private static Stream<Arguments> provideGetOptionTypeRequest(){
        return Stream.of(
                Arguments.of(
                        List.of(new OptionTypes("사이즈"), new OptionTypes("색상"), new OptionTypes("용량")),
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                        null,
                        3,
                        new String[] {"사이즈", "색상", "용량"}
                ),
                Arguments.of(
                        List.of(new OptionTypes("사이즈"), new OptionTypes("색상"), new OptionTypes("용량")),
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                        "사이즈",
                        1,
                        new String[] {"사이즈"}
                ),
                Arguments.of(
                        List.of(new OptionTypes("사이즈1"), new OptionTypes("사이즈2"), new OptionTypes("용량"), new OptionTypes("색상")),
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                        "사이즈",
                        2,
                        new String[] {"사이즈1", "사이즈2"}
                )
        );
    }
}