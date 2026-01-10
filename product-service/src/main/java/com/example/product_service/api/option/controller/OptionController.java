package com.example.product_service.api.option.controller;

import com.example.product_service.api.option.controller.dto.OptionRequest;
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
    public ResponseEntity<OptionResponse> saveOption(@RequestBody @Validated OptionRequest request) {
        OptionResponse response = optionService.saveOption(request.getName(), request.getValues());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{optionId}")
    public ResponseEntity<OptionResponse> getOption(@PathVariable("optionId") Long optionId) {
        OptionResponse response = optionService.getOption(optionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OptionResponse>> getOptions() {
        List<OptionResponse> response = optionService.getOptions();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionResponse> updateOption(@PathVariable("optionId") Long optionId,
                                                       @RequestBody @Validated OptionRequest request) {
        OptionResponse response = optionService.updateOption(optionId, request.getName(), request.getValues());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOption(@PathVariable("optionId") Long optionId) {
        optionService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }
}
