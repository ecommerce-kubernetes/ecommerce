package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.UpdateOptionValueRequest;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.service.OptionValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/option-values")
@Tag(name = "OptionValue", description = "옵션 값 관련 API")
@RequiredArgsConstructor
public class OptionValueController {
    private final OptionValueService optionValueService;

    @AdminApi
    @Operation(summary = "옵션 값 저장")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OptionValuesResponseDto> createOptionValue(@Validated @RequestBody
                                                                     OptionValueRequest requestDto){
        OptionValuesResponseDto responseDto = optionValueService.saveOptionValues(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    //TODO 삭제 예정
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OptionValuesResponseDto> updateOptionValue(@PathVariable("optionValueId") Long optionValueId,
                                                                     @Validated @RequestBody UpdateOptionValueRequest requestDto){
//        OptionValuesResponseDto responseDto = optionValueService.modifyOptionValues(optionValueId, requestDto);

        return ResponseEntity.ok(new OptionValuesResponseDto());

    }

    @AdminApi
    @Operation(summary = "옵션 값 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 완료")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{optionValueId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteOptionValue(@PathVariable("optionValueId") Long optionValueId){
        return ResponseEntity.noContent().build();
    }
}
