package com.example.product_service.service;

import com.example.product_service.dto.request.product.CreateVariantsRequestDto;
import com.example.product_service.dto.request.product.VariantsRequestDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.entity.ProductOptionTypes;
import com.example.product_service.entity.ProductVariants;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.OptionValuesRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService{
    private final ProductsRepository productsRepository;
    private final OptionValuesRepository optionValuesRepository;
    private final ProductVariantsRepository productVariantsRepository;

    @Override
    @Transactional
    public ProductResponseDto addVariants(Long productId, CreateVariantsRequestDto requestDto) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));
        Map<Long, Integer> typeOrder = product.getProductOptionTypes().stream()
                .collect(Collectors.toMap(
                        pot -> pot.getOptionType().getId(),
                        ProductOptionTypes::getPriority
                ));

        for (VariantsRequestDto variant : requestDto.getVariants()) {
            List<Long> requestedOptionValueIds = variant.getOptionValueIds();
            List<OptionValues> optionValues = optionValuesRepository.findAllById(requestedOptionValueIds);
            Set<Long> foundValueIds = optionValues.stream()
                    .map(OptionValues::getId).collect(Collectors.toSet());

            HashSet<Long> missingValueIds = new HashSet<>(requestedOptionValueIds);
            missingValueIds.removeAll(foundValueIds);
            if(!missingValueIds.isEmpty()){
                throw new NotFoundException("Invalid OptionValue Ids : " +missingValueIds);
            }

            Set<Long> allowedTypeIds = typeOrder.keySet();
            Set<Long> invalidTypeValueIds = new HashSet<>();
            for(OptionValues ov : optionValues){
                Long typeId = ov.getOptionType().getId();
                if(!allowedTypeIds.contains(typeId)){
                    invalidTypeValueIds.add(ov.getId());
                }
            }
            if(!invalidTypeValueIds.isEmpty()){
                throw new BadRequestException("OptionValues not match product's OptionTypes: " + invalidTypeValueIds);
            }

            List<OptionValues> sortedValues = optionValues.stream()
                    .sorted(Comparator.comparingInt(
                            ov -> typeOrder.get(ov.getOptionType().getId())
                    ))
                    .toList();

            String sku = buildSku(product.getId(), sortedValues);
            product.addProductVariants(sku, variant.getPrice(), variant.getStockQuantity(),
                    variant.getDiscountValue(), optionValues);
        }
        product = productsRepository.save(product);

        return new ProductResponseDto(product);
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) {
        ProductVariants variant = productVariantsRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Not Found Variant"));

        Products product = variant.getProduct();
        product.removeProductVariants(variant);
    }

    //TODO
    // ProductServiceImpl 동일한 중복 메서드 존재 -> 합칠 예정
    private String buildSku(Long productId, List<OptionValues> sortedValues){
        if(sortedValues == null || sortedValues.isEmpty()){
            return "PRD" + productId;
        }
        String joined = sortedValues.stream()
                .map(OptionValues::getOptionValue)
                .collect(Collectors.joining("-"));
        return String.format("PRD%d-%s",productId, joined);
    }
}
