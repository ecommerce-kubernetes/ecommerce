package com.example.product_service.service;

import com.example.product_service.dto.KafkaDeletedProduct;
import com.example.product_service.dto.KafkaOrderItemDto;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.product.CreateVariantsRequestDto;
import com.example.product_service.dto.request.product.ProductBasicRequestDto;
import com.example.product_service.dto.request.product.ProductRequestDto;
import com.example.product_service.dto.request.product.VariantsRequestDto;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.ProductSummaryDto;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import com.example.product_service.service.client.ImageClientService;
import com.example.product_service.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService{

    private final ProductsRepository productsRepository;
    private final CategoryService categoryService;
    private final OptionTypesRepository optionTypesRepository;
    private final OptionValuesRepository optionValuesRepository;

    private final ImageClientService imageClientService;

    private final KafkaProducer kafkaProducer;
    @Override
    @Transactional
    public ProductResponseDto saveProduct(ProductRequestDto requestDto) {
        Long categoryId = requestDto.getCategoryId();
        List<Long> requestedTypeIds = requestDto.getOptionTypeIds();
        List<VariantsRequestDto> variants = requestDto.getVariants();
        //카테고리 조회
        Categories category = categoryService.getByIdOrThrow(categoryId);
        //상품에 설정하는 카테고리는 가장 하위여야함
        if(!category.isLeaf()){
            throw new BadRequestException("Category must be lowest level");
        }

        /*
        * 상품 엔티티 생성
        * - 이름,
        * - 설명
        * - 카테고리
        * */
        Products product = new Products(
                requestDto.getName(),
                requestDto.getDescription(),
                category
        );
        /*
        * 상품 이미지 추가
        * */
        List<String> imageUrls = requestDto.getImageUrls();
        for(int i=0; i<imageUrls.size(); i++){
            product.addImage(imageUrls.get(i), i);
        }
        /*
            상품 옵션 추가
            상품이 가질 수 있는 옵션
            예) 의류 ( 색상, 사이즈 )
         */
        if(!requestedTypeIds.isEmpty()){
            // 요청 OptionTypeId 존재 검증
            List<OptionTypes> types = optionTypesRepository.findAllById(requestedTypeIds);
            Set<Long> foundTypeIds = types.stream().map(OptionTypes::getId).collect(Collectors.toSet());
            HashSet<Long> missingTypeIds = new HashSet<>(requestedTypeIds);
            missingTypeIds.removeAll(foundTypeIds);
            if(!missingTypeIds.isEmpty()){
                throw new NotFoundException("Invalid OptionType Ids : " + missingTypeIds);
            }
            // OptionTypes 추가
            for (int i = 0; i < types.size(); i++){
                product.addProductOptionTypes(types.get(i), i, true);
            }
        }
        productsRepository.save(product);
        /*
        * 상품 Variants 추가
        * 상품 세부 요소 정보
        * 예) 의류
        * sku(variant 키) : 상품 - 옵션1 - 옵션2 => 티셔츠-M 사이즈-파란색 (TS-M-BLUE)
        * price(가격) : 해당 Variant 의 가격 => 3000원
        * stockQuantity : 해당 Variant 의 수량 => 40개
        * discountValue : 해당 Variant 의 할인율 => 10% 할인
        * optionValue : 해당 Variant 옵션 값 (파란색, M 사이즈)
        * */

        // SKU 를 일정하게 생성하기 위해 <OptionTypeId, 우선순위 시퀀스> 로 Map을 생성
        Map<Long, Integer> typeOrder = product.getProductOptionTypes().stream()
                .collect(Collectors.toMap(
                        pot -> pot.getOptionType().getId(),
                        ProductOptionTypes::getPriority
                ));

        for (VariantsRequestDto variant : variants) {
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

            //OptionValue 를 OptionType 우선순위에 맞게 정렬한 리스트
            List<OptionValues> sortedValues = optionValues.stream()
                    .sorted(Comparator.comparingInt(
                            ov -> typeOrder.get(ov.getOptionType().getId())
                    ))
                    .toList();
            //SKU 생성
            String sku = buildSku(product.getId(), sortedValues);
            product.addProductVariants(sku, variant.getPrice(), variant.getStockQuantity(),
                    variant.getDiscountValue(), optionValues);
        }
        product = productsRepository.save(product);
        return new ProductResponseDto(product);
    }

    @Override
    public PageDto<ProductSummaryDto> getProductList(Pageable pageable, Long categoryId, String name, Integer rating) {
        List<Long> categoryTreeIds = categoryService.getCategoryAndDescendantIds(categoryId);
        Page<ProductSummaryDto> result = productsRepository.findAllByProductSummaryProjection(name, categoryTreeIds, rating, pageable);
        List<ProductSummaryDto> content = result.getContent();
        return new PageDto<>(
                content,
                pageable.getPageNumber(),
                result.getTotalPages(),
                pageable.getPageSize(),
                result.getTotalElements());
    }

    @Override
    public ProductResponseDto getProductDetails(Long productId) {
        Products product = productsRepository.findByIdWithImageAndCategory(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));
        return new ProductResponseDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDto modifyProductBasic(Long productId, ProductBasicRequestDto requestDto) {
        Products product = productsRepository
                .findById(productId).orElseThrow(() -> new NotFoundException("Not Found Product"));
        Categories category = null;
        if(requestDto.getCategoryId() != null){
            category = categoryService.getByIdOrThrow(requestDto.getCategoryId());
            if(!category.isLeaf()){
                throw new BadRequestException("Category must be lowest level");
            }
        }
        product.modifyBasicInfo(requestDto.getName(), requestDto.getDescription(), category);
        return new ProductResponseDto(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));

        List<String> imageUrls = product.getImages().stream().map(ProductImages::getImageUrl).toList();
        imageClientService.deleteImages(imageUrls);
        KafkaDeletedProduct kafkaDeletedProduct = new KafkaDeletedProduct(product.getId());
        kafkaProducer.sendMessage("delete_product", kafkaDeletedProduct);
        productsRepository.delete(product);
    }

    @Override
    @Transactional
    public void batchDeleteProducts(IdsRequestDto requestDto) {
        List<Long> ids = new ArrayList<>(requestDto.getIds());
        List<Products> products = productsRepository.findAllByIdInWithImages(ids);
        List<Long> existIds = products.stream().map(Products::getId).toList();

        ids.removeAll(existIds);
        if(!ids.isEmpty()){
            throw new NotFoundException("Not Found Products ids : " + ids);
        }

        for (Products product : products) {
            List<String> imageUrls = product.getImages().stream().map(ProductImages::getImageUrl).toList();
            imageClientService.deleteImages(imageUrls);
            KafkaDeletedProduct deletedProductMessage = new KafkaDeletedProduct(product.getId());
            kafkaProducer.sendMessage("delete_product", deletedProductMessage);
        }
        productsRepository.deleteAll(products);
    }

    @Override
    @Transactional
    public ProductResponseDto modifyStockQuantity(Long productId, StockQuantityRequestDto stockQuantityRequestDto) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));
        int updateStockQuantity = stockQuantityRequestDto.getUpdateStockQuantity();

        return new ProductResponseDto(product);
    }

    @Override
    public List<CompactProductResponseDto> getProductListByIds(ProductRequestIdsDto productRequestIdsDto) {
        List<Long> ids = productRequestIdsDto.getIds();

        List<CompactProductResponseDto> findProducts = productsRepository.findAllWithRepresentativeImageByIds(ids);

        Set<Long> findProductsId = findProducts.stream()
                .map(CompactProductResponseDto::getId)
                .collect(Collectors.toSet());

        List<Long> foundIds = ids.stream().filter(id -> !findProductsId.contains(id)).toList();

        if(!foundIds.isEmpty()){
            throw new NotFoundException("Not Found product by id:" + foundIds);
        }

        return findProducts;
    }

    @Override
    @Transactional
    public void decrementStockQuantity(List<KafkaOrderItemDto> orderedItems) {
        for (KafkaOrderItemDto orderedItem : orderedItems) {
            Products products = productsRepository.findById(orderedItem.getProductId())
                    .orElseThrow(() -> new NotFoundException("Not Found Product"));

        }
    }

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
