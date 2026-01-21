package com.example.product_service.api.option.controller;

import com.example.product_service.api.option.controller.dto.OptionCreateRequest;
import com.example.product_service.api.option.controller.dto.OptionUpdateRequest;
import com.example.product_service.api.option.service.OptionService;
import com.example.product_service.api.option.service.dto.OptionResponse;
import com.example.product_service.api.option.service.dto.OptionValueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @PostMapping("/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionResponse> saveOption(@RequestBody @Validated OptionCreateRequest request) {
        OptionResponse response = optionService.saveOption(request.getName(), request.getValues());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/options/{optionTypeId}")
    public ResponseEntity<OptionResponse> getOption(@PathVariable("optionTypeId") Long optionTypeId) {
        OptionResponse response = optionService.getOption(optionTypeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/options")
    public ResponseEntity<List<OptionResponse>> getOptions() {
        List<OptionResponse> response = optionService.getOptions();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/options/{optionTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionResponse> updateOptionType(@PathVariable("optionTypeId") Long optionTypeId,
                                                           @RequestBody @Validated OptionUpdateRequest request) {
        OptionResponse response = optionService.updateOptionTypeName(optionTypeId, request.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/options/{optionTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOption(@PathVariable("optionTypeId") Long optionTypeId) {
        optionService.deleteOption(optionTypeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/option-values/{optionValueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionValueResponse> updateOptionValue(@PathVariable("optionValueId") Long optionValueId,
                                                                 @RequestBody @Validated OptionUpdateRequest request) {
        OptionValueResponse response = optionService.updateOptionValueName(optionValueId, request.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/option-values/{optionValueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOptionValue(@PathVariable("optionValueId") Long optionValueId) {
        optionService.deleteOptionValue(optionValueId);
        return ResponseEntity.noContent().build();
    }
}
