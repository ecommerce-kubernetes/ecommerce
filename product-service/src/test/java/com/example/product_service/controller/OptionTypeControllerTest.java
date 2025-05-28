package com.example.product_service.controller;

import com.example.product_service.common.advice.dto.DetailError;
import com.example.product_service.controller.util.ControllerResponseValidator;
import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionTypesRequestDto;
import com.example.product_service.dto.response.options.OptionTypesResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.OptionTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionTypeController.class)
@Import(ControllerResponseValidator.class)
@Slf4j
class OptionTypeControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    OptionTypeService optionTypeService;
    @Autowired
    ControllerResponseValidator validator;

    ObjectMapper mapper = new ObjectMapper();

    final static String BAD_REQUEST = "BadRequest";
    final static String CONFLICT = "Conflict";

    final static String BAD_REQUEST_MESSAGE = "Validation Error";

    /*
        상품 옵션 타입 저장
        상품 옵션 예시 (사이즈[Size], 색상[Color], 용량[Capacity])
     */
    @Test
    @DisplayName("OptionTypes 등록 테스트")
    void optionTypeRegisterTest() throws Exception {
        //옵션 타입 Request, Response 생성
        OptionTypesRequestDto requestDto = new OptionTypesRequestDto("사이즈");
        OptionTypesResponseDto responseDto = new OptionTypesResponseDto(1L, requestDto.getName());

        //요청 Body 변환
        String content = mapper.writeValueAsString(requestDto);

        //optionTypeService Mocking
        when(optionTypeService.saveOptionTypes(any(OptionTypesRequestDto.class))).thenReturn(responseDto);

        //Test

        //1. 요청
        ResultActions perform =
                mockMvc.perform(post("/option-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content));

        //2. 검증
        /*
            - 상태코드
            - id 필드
            - name 필드
         */
        perform.andExpect(status().isCreated());
        validator.validResponse(perform,OptionTypesResponseDto.class, responseDto);

    }

    @Test
    @DisplayName("OptionTypes 등록 테스트_입력값 검증")
    void optionTypeRegisterTest_InvalidOptionTypeRequestDto() throws Exception {
        String requestUrl = "/option-types";

        // 옵션 타입 - name = null;
        OptionTypesRequestDto requestDto = new OptionTypesRequestDto();
        // 옵션 Body 변환
        String content = mapper.writeValueAsString(requestDto);

        //Test

        //1. 요청
        ResultActions perform = mockMvc.perform(post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
        // 검증 필드
        DetailError detailError = new DetailError("name", "name is required");

        //2. 검증
        /*
            - 상태코드
            - 에러메시지
            - 세부 에러메시지
            - 에러 필드
            - 요청 URL
         */
        validator.validInvalidFields(perform, HttpStatus.SC_BAD_REQUEST, BAD_REQUEST, BAD_REQUEST_MESSAGE, List.of(detailError), requestUrl);

    }

    @Test
    @DisplayName("OptionTypes 저장 테스트_중복 이름 저장")
    void optionTypeRegisterTest_NameConflict() throws Exception {
        String requestUrl = "/option-types";
        String expectedMessage = "OptionTypes name Conflict";

        // 옵션 타입 - name = 중복값;
        OptionTypesRequestDto requestDto = new OptionTypesRequestDto("중복 이름");
        // 옵션 Body 변환
        String content = mapper.writeValueAsString(requestDto);

        //optionTypeService 모킹
        when(optionTypeService.saveOptionTypes(any(OptionTypesRequestDto.class)))
                .thenThrow(new DuplicateResourceException("OptionTypes name Conflict"));

        //Test
        //1. 요청
        ResultActions perform =
                mockMvc.perform(post(requestUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content));

        //2. 검증
        validator.verifyErrorResponse(perform, HttpStatus.SC_CONFLICT, CONFLICT, expectedMessage, requestUrl);
    }

    @Test
    @DisplayName("OptionTypes 조회 테스트")
    void optionTypesTest() throws Exception {
        String requestUrl = "/option-types";
        // response
        PageDto<OptionTypesResponseDto> responseDto = new PageDto<>(
                List.of(new OptionTypesResponseDto(1L, "사이즈"),
                        new OptionTypesResponseDto(2L, "용량")),
                0,
                1,
                10,
                2
        );

        when(optionTypeService.getOptionTypes(any(), any(Pageable.class))).thenReturn(responseDto);
        ResultActions perform = mockMvc.perform(get(requestUrl));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPage").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElement").value(2));

        List<OptionTypesResponseDto> content = responseDto.getContent();
        for(int i=0; i<content.size(); i++){
            perform
                    .andExpect(jsonPath("$.content[" + i +"].id").value(content.get(i).getId()))
                    .andExpect(jsonPath("$.content[" + i + "].name").value(content.get(i).getName()));
        }
    }

    @Test
    @DisplayName("OptionTypes 변경 테스트")
    void updateOptionTypeTest() throws Exception {
        Long targetId = 1L;
        String requestUrl = "/option-types/" + targetId;
        //변경 request
        OptionTypesRequestDto requestDto = new OptionTypesRequestDto("색상");
        //response
        OptionTypesResponseDto responseDto = new OptionTypesResponseDto(targetId,"색상");
        //request 변환
        String content = mapper.writeValueAsString(requestDto);

        //optionTypeService 모킹
        when(optionTypeService.modifyOptionTypes(anyLong(), any(OptionTypesRequestDto.class)))
                .thenReturn(responseDto);

        ResultActions perform = mockMvc.perform(patch(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        //Verifying
        perform.andExpect(status().isOk());
        validator.validResponse(perform, OptionTypesResponseDto.class, responseDto);
    }

    @Test
    @DisplayName("OptionTypes 단일 삭제 테스트")
    void deleteOptionTypeTest() throws Exception {
        Long targetId = 1L;
        String requestUrl = "/option-types/" + targetId;

        //optionTypeService 모킹
        doNothing().when(optionTypeService).deleteOptionTypes(targetId);

        ResultActions perform = mockMvc.perform(delete(requestUrl));

        perform.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("OptionTypes 단일 삭제 테스트 _ optionType 을 찾을 수 없을때")
    void deleteOptionTypeTest_NotFoundOptionTypes() throws Exception {
        Long targetId = 999L;
        String requestUrl = "/option-types/" + targetId;

        //optionService 모킹
        doThrow(new NotFoundException("Not Found OptionTypes")).when(optionTypeService).deleteOptionTypes(targetId);

        ResultActions perform = mockMvc.perform(delete(requestUrl));

        validator.verifyErrorResponse(perform, HttpStatus.SC_NOT_FOUND, "NotFound",
                "Not Found OptionTypes", requestUrl);
    }

    @Test
    @DisplayName("OptionTypes 배치 삭제 테스트")
    void optionTypeBatchDeleteTest() throws Exception {
        String requestUrl = "/option-types/batch-delete";
        IdsRequestDto requestIdsDto = new IdsRequestDto(List.of(1L, 2L));

        String content = mapper.writeValueAsString(requestIdsDto);

        //optionTypeService 모킹
        doNothing().when(optionTypeService).batchDeleteOptionTypes(any(IdsRequestDto.class));

        ResultActions perform = mockMvc.perform(post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        perform
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("OptionTypes 배치 삭제 테스트 _ 존재하지 않는 id 포함시")
    void optionTypesBatchDeleteTest_NotFoundIds() throws Exception {
        String requestUrl = "/option-types/batch-delete";
        IdsRequestDto requestIdsDto = new IdsRequestDto(List.of(1L, 2L));

        String content = mapper.writeValueAsString(requestIdsDto);

        //optionTypeService 모킹
        doThrow(new NotFoundException("Not Found OptionType ids : [1]"))
                .when(optionTypeService).batchDeleteOptionTypes(any(IdsRequestDto.class));

        ResultActions perform =
                mockMvc.perform(post(requestUrl).contentType(MediaType.APPLICATION_JSON).content(content));

        validator.verifyErrorResponse(perform, HttpStatus.SC_NOT_FOUND,
                "NotFound",
                "Not Found OptionType ids : [1]",
                requestUrl);
    }

    @Test
    @DisplayName("OptionValues 조회")
    void getValuesByTypeTest() throws Exception {
        Long optionTypeId = 1L;
        String requestUrl = "/option-types/"+optionTypeId+"/option-values";

        List<OptionValuesResponseDto> responseDto = List.of(new OptionValuesResponseDto(1L, "XL", optionTypeId),
                new OptionValuesResponseDto(2L, "L", optionTypeId));

        when(optionTypeService.getOptionValuesByTypeId(anyLong())).thenReturn(responseDto);

        ResultActions perform = mockMvc.perform(get(requestUrl));

        perform.andExpect(status().isOk());
        validator.validResponse(perform, OptionValuesResponseDto.class, responseDto);
    }

}