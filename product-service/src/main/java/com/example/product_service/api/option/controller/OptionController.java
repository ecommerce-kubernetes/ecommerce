package com.example.product_service.api.option.controller;

import com.example.product_service.api.option.controller.dto.request.OptionRequest;
import com.example.product_service.api.option.controller.dto.request.OptionRequest.Create;
import com.example.product_service.api.option.controller.dto.response.OptionResponse;
import com.example.product_service.api.option.service.OptionService;
import com.example.product_service.api.option.service.dto.command.OptionCommand;
import com.example.product_service.api.option.service.dto.result.OptionResult;
import com.example.product_service.api.option.service.dto.result.OptionValueResult;
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
    public ResponseEntity<OptionResponse.Detail> saveOption(@RequestBody @Validated Create request) {
        OptionCommand.Create command = request.toCommand();
        OptionResult result = optionService.saveOption(command);
        OptionResponse.Detail response = OptionResponse.Detail.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/options/{optionTypeId}")
    public ResponseEntity<OptionResponse.Detail> getOption(@PathVariable("optionTypeId") Long optionTypeId) {
        OptionResult result = optionService.getOption(optionTypeId);
        OptionResponse.Detail response = OptionResponse.Detail.from(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/options")
    public ResponseEntity<List<OptionResponse.Detail>> getOptions() {
        List<OptionResult> results = optionService.getOptions();
        List<OptionResponse.Detail> responses = OptionResponse.Detail.from(results);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/options/{optionTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptionResponse.Detail> updateOptionType(@PathVariable("optionTypeId") Long optionTypeId,
                                                         @RequestBody @Validated OptionRequest.UpdateOptionType request) {
        OptionCommand.UpdateOptionType command = request.toCommand();
        OptionResult result = optionService.updateOptionTypeName(command);
        OptionResponse.Detail response = OptionResponse.Detail.from(result);
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
                                                               @RequestBody @Validated OptionRequest.UpdateOptionValue request) {
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
