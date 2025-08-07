package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.*;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.options.OptionValuesResponse;
import com.example.product_service.service.OptionTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "OptionType", description = "옵션 타입 관련 API")
@RequestMapping("/option-types")
public class OptionTypeController {

    private final OptionTypeService optionTypeService;

    @AdminApi
    @Operation(summary = "옵션 타입 저장")
    @ApiResponse(responseCode = "201", description = "저장 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @ConflictApiResponse
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OptionTypeResponse> createOptionType(@Validated @RequestBody
                                                                   OptionTypeRequest request){
        OptionTypeResponse response = optionTypeService.saveOptionTypes(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "옵션 타입 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<OptionTypeResponse>> getOptionTypes(){
        List<OptionTypeResponse> response = optionTypeService.getOptionTypes();
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "옵션 타입 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/{optionTypeId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OptionTypeResponse> updateOptionType(@PathVariable("optionTypeId") Long optionTypeId,
                                                               @Validated @RequestBody OptionTypeRequest request){
        OptionTypeResponse response = optionTypeService.updateOptionTypeById(optionTypeId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "옵션 타입 값 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{optionTypeId}/values")
    public ResponseEntity<List<OptionValuesResponse>> getValuesByType(@PathVariable("optionTypeId") Long optionTypeId){
        List<OptionValuesResponse> response = optionTypeService.getOptionValuesByTypeId(optionTypeId);
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "옵션 타입 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{optionTypeId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteOptionType(@PathVariable("optionTypeId") Long optionTypeId){
        optionTypeService.deleteOptionTypeById(optionTypeId);
        return ResponseEntity.noContent().build();
    }
}
