package com.example.product_service.api.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/variants")
public class ProductInternalController {

    @GetMapping("/{variantId}")
    public ResponseEntity getVariant(@PathVariable("variantId") Long variantId) {
        return null;
    }

    @PostMapping("/by-ids")
    public ResponseEntity getVariants(@RequestBody List<Long> ids) {
        return null;
    }
}
