package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionValuesRequestDto;
import com.example.product_service.dto.request.options.OptionValuesUpdateRequestDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.service.OptionValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/option-values")
@Tag(name = "OptionType", description = "옵션 타입 관련 API")
@RequiredArgsConstructor
public class OptionValueController {
    private final OptionValueService optionValueService;

    @AdminApi
    @Operation(summary = "옵션 값 저장")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping
    public ResponseEntity<OptionValuesResponseDto> createOptionValue(@Validated @RequestBody
                                                                           OptionValuesRequestDto requestDto){
        OptionValuesResponseDto responseDto = optionValueService.saveOptionValues(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Void> optionValueBatchDelete(@Validated @RequestBody IdsRequestDto requestDto){
        optionValueService.batchDeleteOptionValues(requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @AdminApi
    @Operation(summary = "옵션 값 수정")
    @ApiResponse(responseCode = "200", description = "수정 완료")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/{optionValueId}")
    public ResponseEntity<OptionValuesResponseDto> updateOptionValue(@PathVariable("optionValueId") Long optionValueId,
                                                                     @Validated @RequestBody OptionValuesUpdateRequestDto requestDto){
        OptionValuesResponseDto responseDto = optionValueService.modifyOptionValues(optionValueId, requestDto);

        return ResponseEntity.ok(responseDto);

    }

    @AdminApi
    @Operation(summary = "옵션 값 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 완료")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{optionValueId}")
    public ResponseEntity<Void> deleteOptionValue(@PathVariable("optionValueId") Long optionValueId){
        return ResponseEntity.noContent().build();
    }
}
