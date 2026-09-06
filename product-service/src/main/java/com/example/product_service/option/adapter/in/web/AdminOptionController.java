package com.example.product_service.option.adapter.in.web;

import com.example.product_service.option.adapter.in.web.dto.request.AddOptionValueRequest;
import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;
import com.example.product_service.option.adapter.in.web.dto.response.*;
import com.example.product_service.option.application.service.OptionCommandService;
import com.example.product_service.option.application.service.OptionQueryService;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.example.product_service.option.application.service.dto.result.OptionTypesResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminOptionController {

    private final OptionCommandService optionCommandService;

    private final OptionQueryService optionQueryService;

    @PostMapping("/option-types")
    public ResponseEntity<CreateOptionTypeResponse> createOptionType(@RequestBody @Validated CreateOptionTypeRequest request) {
        CreateOptionTypeCommand command = request.toCommand();
        Long id = optionCommandService.createOptionType(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateOptionTypeResponse.of(id));
    }

    @GetMapping("/option-types")
    public ResponseEntity<OptionTypesResponse> getOptionTypes() {
        OptionTypesResult types = optionQueryService.getTypes();
        return ResponseEntity.ok(OptionTypesResponse.from(types));
    }

    @GetMapping("/option-types/{optionTypeId}")
    public ResponseEntity<OptionTypeResponse> getOptionType(@PathVariable("optionTypeId") Long optionTypeId) {
        OptionTypeResult type = optionQueryService.getType(optionTypeId);
        return ResponseEntity.ok(OptionTypeResponse.from(type));
    }

    @PatchMapping("/option-types/{optionTypeId}")
    public ResponseEntity<UpdateOptionTypeResponse> updateOptionType(@PathVariable("optionTypeId") Long optionTypeId,
                                                                     @RequestBody @Validated UpdateOptionTypeRequest request) {
        UpdateOptionTypeCommand command = request.toCommand(optionTypeId);
        Long id = optionCommandService.updateOptionType(command);
        return ResponseEntity.ok(UpdateOptionTypeResponse.of(id));
    }

    @DeleteMapping("/option-types/{optionTypeId}")
    public ResponseEntity<Void> deleteOptionType(@PathVariable("optionTypeId") Long optionTypeId) {
        return null;
    }

    @PostMapping("/option-types/{optionTypeId}/values")
    public ResponseEntity<AddOptionValueResponse> addOptionValues(@PathVariable("optionTypeId") Long optionTypeId,
                                                                  @RequestBody @Validated AddOptionValueRequest request) {
        return null;
    }

    @PatchMapping("option-types/{optionTypeId}/values/{optionValueId}")
    public ResponseEntity<UpdateOptionValueResponse> updateOptionValue(@PathVariable("optionTypeId") Long optionTypeId,
                                                                       @PathVariable("optionValueId") Long optionValueId,
                                                                       @RequestBody @Validated UpdateOptionValueRequest request) {
        return null;
    }

    @DeleteMapping("/option-types/{optionTypeId}/values/{optionValueId}")
    public ResponseEntity<Void> deleteOptionValue(@PathVariable("optionTypeId") Long optionTypeId,
                                                  @PathVariable("optionValueId") Long optionValueId) {
        return null;
    }
}
