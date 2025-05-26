package com.example.product_service.controller;

import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionTypesRequestDto;
import com.example.product_service.dto.response.options.OptionTypesResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.service.OptionTypeService;
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
@Slf4j
@RequestMapping("/option-types")
public class OptionTypeController {

    private final OptionTypeService optionTypeService;

    @PostMapping
    public ResponseEntity<OptionTypesResponseDto> optionTypeRegister(@Validated @RequestBody
                                                                         OptionTypesRequestDto requestDto){
        OptionTypesResponseDto responseDto = optionTypeService.saveOptionTypes(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<PageDto<OptionTypesResponseDto>> optionTypes(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "query" ,required = false) String query){
        PageDto<OptionTypesResponseDto> optionTypes = optionTypeService.getOptionTypes(query, pageable);
        return ResponseEntity.ok(optionTypes);
    }

    @PatchMapping("/{optionTypeId}")
    public ResponseEntity<OptionTypesResponseDto> updateOptionType(@PathVariable("optionTypeId") Long optionTypeId,
                                                                   @Validated @RequestBody OptionTypesRequestDto requestDto){
        OptionTypesResponseDto responseDto = optionTypeService.modifyOptionTypes(optionTypeId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{optionTypeId}/option-values")
    public ResponseEntity<List<OptionValuesResponseDto>> getValuesByType(@PathVariable("optionTypeId") Long optionTypeId){
        List<OptionValuesResponseDto> response = optionTypeService.getOptionValuesByTypeId(optionTypeId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{optionTypeId}")
    public ResponseEntity<Void> deleteOptionType(@PathVariable("optionTypeId") Long optionTypeId){
        optionTypeService.deleteOptionTypes(optionTypeId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Void> optionTypeBatchDelete(@Validated @RequestBody IdsRequestDto requestDto){
        optionTypeService.batchDeleteOptionTypes(requestDto);

        return ResponseEntity.noContent().build();
    }

}
