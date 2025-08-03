package com.example.product_service.controller;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import com.example.product_service.controller.util.specification.annotation.BadRequestApiResponse;
import com.example.product_service.controller.util.specification.annotation.ForbiddenApiResponse;
import com.example.product_service.controller.util.specification.annotation.NotFoundApiResponse;
import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionTypesRequestDto;
import com.example.product_service.dto.response.options.OptionTypesResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.service.OptionTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @BadRequestApiResponse @ForbiddenApiResponse
    @PostMapping
    public ResponseEntity<OptionTypesResponseDto> createOptionType(@Validated @RequestBody
                                                                         OptionTypesRequestDto requestDto){
        OptionTypesResponseDto responseDto = optionTypeService.saveOptionTypes(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "옵션 타입 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<OptionTypesResponseDto>> optionTypes(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "query" ,required = false) String query){
//        PageDto<OptionTypesResponseDto> optionTypes = optionTypeService.getOptionTypes(query, pageable);
        return ResponseEntity.ok(List.of(new OptionTypesResponseDto()));
    }

    @AdminApi
    @Operation(summary = "옵션 타입 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @BadRequestApiResponse @ForbiddenApiResponse @NotFoundApiResponse
    @PatchMapping("/{optionTypeId}")
    public ResponseEntity<OptionTypesResponseDto> updateOptionType(@PathVariable("optionTypeId") Long optionTypeId,
                                                                   @Validated @RequestBody OptionTypesRequestDto requestDto){
        OptionTypesResponseDto responseDto = optionTypeService.modifyOptionTypes(optionTypeId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "옵션 타입 값 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @NotFoundApiResponse
    @GetMapping("/{optionTypeId}/values")
    public ResponseEntity<List<OptionValuesResponseDto>> getValuesByType(@PathVariable("optionTypeId") Long optionTypeId){
        List<OptionValuesResponseDto> response = optionTypeService.getOptionValuesByTypeId(optionTypeId);
        return ResponseEntity.ok(response);
    }

    @AdminApi
    @Operation(summary = "옵션 타입 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ForbiddenApiResponse @NotFoundApiResponse
    @DeleteMapping("/{optionTypeId}")
    public ResponseEntity<Void> deleteOptionType(@PathVariable("optionTypeId") Long optionTypeId){
        optionTypeService.deleteOptionTypes(optionTypeId);

        return ResponseEntity.noContent().build();
    }

    //TODO 삭제 예정
    @PostMapping("/batch-delete")
    public ResponseEntity<Void> optionTypeBatchDelete(@Validated @RequestBody IdsRequestDto requestDto){
        optionTypeService.batchDeleteOptionTypes(requestDto);

        return ResponseEntity.noContent().build();
    }

}
