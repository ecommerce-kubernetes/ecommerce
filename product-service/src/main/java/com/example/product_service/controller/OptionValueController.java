package com.example.product_service.controller;

import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.options.OptionValuesRequestDto;
import com.example.product_service.dto.request.options.OptionValuesUpdateRequestDto;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.service.OptionValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/option-values")
@RequiredArgsConstructor
public class OptionValueController {
    private final OptionValueService optionValueService;

    @PostMapping
    public ResponseEntity<OptionValuesResponseDto> optionValueRegister(@Validated @RequestBody
                                                                           OptionValuesRequestDto requestDto){
        OptionValuesResponseDto responseDto = optionValueService.saveOptionValues(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Void> optionValueBatchDelete(@Validated @RequestBody IdsRequestDto requestDto){
        optionValueService.batchDeleteOptionValues(requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{optionValueId}")
    public ResponseEntity<OptionValuesResponseDto> updateOptionValue(@PathVariable("optionValueId") Long optionValueId,
                                                                     @Validated @RequestBody OptionValuesUpdateRequestDto requestDto){
        OptionValuesResponseDto responseDto = optionValueService.modifyOptionValues(optionValueId, requestDto);

        return ResponseEntity.ok(responseDto);

    }
}
