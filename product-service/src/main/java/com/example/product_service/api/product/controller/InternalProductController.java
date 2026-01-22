package com.example.product_service.api.product.controller;

import com.example.product_service.api.product.controller.dto.InternalVariantRequest;
import com.example.product_service.api.product.service.VariantService;
import com.example.product_service.api.product.service.dto.result.InternalVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/variants")
@RequiredArgsConstructor
public class InternalProductController {
    private final VariantService variantService;

    @GetMapping("/{variantId}")
    public ResponseEntity<InternalVariantResponse> getVariant(@PathVariable("variantId") Long variantId) {
        InternalVariantResponse response = variantService.getVariant(variantId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<InternalVariantResponse>> getVariants(@RequestBody @Validated InternalVariantRequest request) {
        List<InternalVariantResponse> response = variantService.getVariants(request.getVariantIds());
        return ResponseEntity.ok(response);
    }
}
