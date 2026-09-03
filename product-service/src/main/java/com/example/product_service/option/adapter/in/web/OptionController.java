package com.example.product_service.option.adapter.in.web;

import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;
import com.example.product_service.option.adapter.in.web.dto.response.OptionDetailResponse;
import com.example.product_service.option.application.service.OptionService;
import com.example.product_service.option.application.service.dto.command.OptionCommand;
import com.example.product_service.option.application.service.dto.result.OptionResult;
import com.example.product_service.option.application.service.dto.result.OptionValueResult;
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
    public ResponseEntity<OptionDetailResponse> saveOption(@RequestBody @Validated CreateOptionRequest request) {
        OptionCommand.Create command = request.toCommand();
        OptionResult result = optionService.saveOption(command);
        OptionDetailResponse response = OptionDetailResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/options/{optionTypeId}")
    public ResponseEntity<OptionDetailResponse> getOption(@PathVariable("optionTypeId") Long optionTypeId) {
        OptionResult result = optionService.getOption(optionTypeId);
        OptionDetailResponse response = OptionDetailResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/options")
    public ResponseEntity<List<OptionDetailResponse>> getOptions() {
        List<OptionResult> results = optionService.getOptions();
        List<OptionDetailResponse> responses = OptionDetailResponse.from(results);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/options/{optionTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionDetailResponse> updateOptionType(@PathVariable("optionTypeId") Long optionTypeId,
                                                         @RequestBody @Validated UpdateOptionTypeRequest request) {
        OptionCommand.UpdateOptionType command = request.toCommand();
        OptionResult result = optionService.updateOptionTypeName(command);
        OptionDetailResponse response = OptionDetailResponse.from(result);
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
    public ResponseEntity<OptionValueResult> updateOptionValue(@PathVariable("optionValueId") Long optionValueId,
                                                               @RequestBody @Validated UpdateOptionValueRequest request) {
        OptionCommand.UpdateOptionValue command = request.toCommand();
        OptionValueResult response = optionService.updateOptionValueName(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/option-values/{optionValueId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOptionValue(@PathVariable("optionValueId") Long optionValueId) {
        optionService.deleteOptionValue(optionValueId);
        return ResponseEntity.noContent().build();
    }
}
