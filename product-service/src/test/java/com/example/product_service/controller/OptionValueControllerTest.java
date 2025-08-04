package com.example.product_service.controller;

import com.example.product_service.controller.util.ControllerResponseValidator;
import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.UpdateOptionValueRequest;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.OptionValueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionValueController.class)
@Import(ControllerResponseValidator.class)
class OptionValueControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ControllerResponseValidator validator;
    @MockitoBean
    OptionValueService optionValueService;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("OptionValues 등록 테스트")
    void optionValueRegisterTest() throws Exception {
        String requestUrl = "/option-values";
        //옵션 Value request, response 생성
        String optionValue = "XL";
        OptionValueRequest requestDto = new OptionValueRequest(1L, optionValue);
        OptionValuesResponseDto responseDto = new OptionValuesResponseDto(1L , optionValue, 1L);

        //request body 변환
        String content = mapper.writeValueAsString(requestDto);

        //optionValueService Mocking
        when(optionValueService.saveOptionValues(any(OptionValueRequest.class)))
                .thenReturn(responseDto);

        //Test

        ResultActions perform =
                mockMvc.perform(post(requestUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content));
        //검증
        validator.validResponse(perform, OptionValuesResponseDto.class, responseDto);
    }

    @Test
    @DisplayName("OptionValues 배치 삭제 테스트")
    void optionValueBatchDeleteTest() throws Exception {
        String requestUrl = "/option-values/batch-delete";
        IdsRequestDto idsRequestDto = new IdsRequestDto(List.of(1L, 2L));

        String content = mapper.writeValueAsString(idsRequestDto);

        doNothing().when(optionValueService).batchDeleteOptionValues(any(IdsRequestDto.class));

        ResultActions perform = mockMvc.perform(post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON).content(content));

        perform.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("OptionValues 배치 삭제 테스트 _ 존재하지 않는 id 포함시")
    void optionValueBatchDeleteTest_NotFoundIds() throws Exception {
        String requestUrl = "/option-values/batch-delete";
        IdsRequestDto idsRequestDto = new IdsRequestDto(List.of(1L, 2L));

        String content = mapper.writeValueAsString(idsRequestDto);

        doThrow(new NotFoundException("Not Found OptionValue ids : [1]"))
                .when(optionValueService).batchDeleteOptionValues(any(IdsRequestDto.class));

        ResultActions perform =
                mockMvc.perform(post(requestUrl)
                        .contentType(MediaType.APPLICATION_JSON).content(content));

        validator.verifyErrorResponse(perform, HttpStatus.SC_NOT_FOUND,
                "NotFound",
                "Not Found OptionValue ids : [1]",
                requestUrl);
    }

//    @Test
//    @DisplayName("OptionValues 변경 테스트")
//    void updateOptionValueTest() throws Exception {
//        String modifyOptionValue="L";
//        Long optionValueId = 1L;
//        String requestUrl = "/option-values/" + optionValueId;
//        UpdateOptionValueRequest requestDto = new UpdateOptionValueRequest(modifyOptionValue);
//        OptionValuesResponseDto responseDto = new OptionValuesResponseDto(optionValueId, modifyOptionValue, 1L);
//
//        String content = mapper.writeValueAsString(requestDto);
//
//        when(optionValueService.modifyOptionValues(anyLong(),any(UpdateOptionValueRequest.class)))
//                .thenReturn(responseDto);
//
//
//        ResultActions perform =
//                mockMvc.perform(patch(requestUrl).contentType(MediaType.APPLICATION_JSON).content(content));
//
//        validator.validResponse(perform, OptionValuesResponseDto.class, responseDto);
//    }

//    @Test
//    @DisplayName("OptionValues 변경 테스트_ 없는 Id")
//    void updateOptionValuesTest_NotFound() throws Exception {
//        String modifyOptionValue="L";
//        Long optionValueId = 1L;
//        String requestUrl = "/option-values/" + optionValueId;
//        UpdateOptionValueRequest requestDto = new UpdateOptionValueRequest(modifyOptionValue);
//
//        String content = mapper.writeValueAsString(requestDto);
//
//        when(optionValueService.modifyOptionValues(anyLong(), any(UpdateOptionValueRequest.class)))
//                .thenThrow(new NotFoundException("Not Found OptionValue"));
//
//        ResultActions perform =
//                mockMvc.perform(patch(requestUrl).contentType(MediaType.APPLICATION_JSON).content(content));
//
//        validator.verifyErrorResponse(perform, HttpStatus.SC_NOT_FOUND, "NotFound", "Not Found OptionValue", requestUrl);
//    }

}