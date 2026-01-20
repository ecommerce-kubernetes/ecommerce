package com.example.product_service.api.option.controller;

import com.example.product_service.api.option.controller.dto.OptionCreateRequest;
import com.example.product_service.api.option.controller.dto.OptionUpdateRequest;
import com.example.product_service.api.option.service.OptionService;
import com.example.product_service.api.option.service.dto.OptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/options")
public class OptionController {

    private final OptionService optionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionResponse> saveOption(@RequestBody @Validated OptionCreateRequest request) {
        OptionResponse response = optionService.saveOption(request.getName(), request.getValues());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{typeId}")
    public ResponseEntity<OptionResponse> getOption(@PathVariable("typeId") Long typeId) {
        OptionResponse response = optionService.getOption(typeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OptionResponse>> getOptions() {
        List<OptionResponse> response = optionService.getOptions();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionResponse> updateOptionType(@PathVariable("typeId") Long typeId,
                                                           @RequestBody @Validated OptionUpdateRequest request) {
        OptionResponse response = optionService.updateOptionTypeName(typeId, request.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOption(@PathVariable("typeId") Long typeId) {
        optionService.deleteOption(typeId);
        return ResponseEntity.noContent().build();
    }
}
