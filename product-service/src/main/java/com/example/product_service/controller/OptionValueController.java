package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.*;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.service.OptionValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "OptionValue", description = "옵션 값 관련 API")
@RequestMapping("/option-values")
@RequiredArgsConstructor
public class OptionValueController {
    private final OptionValueService optionValueService;

    @Operation(summary = "옵션 값 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{optionValueId}")
    public ResponseEntity<OptionValueResponse> getOptionValue(@PathVariable("optionValueId") Long optionValueId){

        OptionValueResponse response = optionValueService.getOptionValueById(optionValueId);
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "옵션 값 수정")
    @ApiResponse(responseCode = "200", description = "수정 완료")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse @ConflictApiResponse
    @PatchMapping("/{optionValueId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OptionValueResponse> updateOptionValue(@PathVariable("optionValueId") Long optionValueId,
                                                                  @Validated @RequestBody OptionValueRequest request){
        OptionValueResponse response = optionValueService.updateOptionValueById(optionValueId, request);
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "옵션 값 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 완료")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{optionValueId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteOptionValue(@PathVariable("optionValueId") Long optionValueId){
        optionValueService.deleteOptionValueById(optionValueId);
        return ResponseEntity.noContent().build();
    }
}
