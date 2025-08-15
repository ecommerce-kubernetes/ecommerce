package com.example.product_service.service;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import com.example.product_service.service.util.ProductRequestStructureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.product_service.common.MessagePath.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductsRepository productsRepository;
    private final CategoryRepository categoryRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final ProductRequestStructureValidator structureValidator;
    private final MessageSourceUtil ms;

    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        //요청 바디 유효성 검사
        structureValidator.validateProductRequest(request);
        //SKU 중복 검사
        validateDuplicateSkus(request);

        Categories category = findCategoryByIdOrThrow(request.getCategoryId());
        //상품 옵션 타입 조회
        List<OptionTypes> optionTypes = findOptionTypes(request);
        Map<Long, OptionTypes> optionTypeById = optionTypes.stream()
                .collect(Collectors.toMap(OptionTypes::getId, Function.identity()));

        Map<Long, Set<Long>> optionTypeToValueIds = optionTypes.stream()
                .collect(Collectors.toMap(OptionTypes::getId, ot -> ot.getOptionValues().stream().map(OptionValues::getId).collect(Collectors.toSet())));
        //상품 변형 옵션 값 옵션 타입의 연관 객체인지 검증
        validateOptionValueCardinality(request, optionTypeToValueIds);


        Products products = new Products(request.getName(), request.getDescription(), category);
        for(ImageRequest imageRequest : request.getImages()){
            ProductImages productImage = new ProductImages(imageRequest.getUrl(), imageRequest.getSortOrder());
            products.addImage(productImage);
        }

        for(ProductOptionTypeRequest productOptionTypeRequest : request.getProductOptionTypes()){
            OptionTypes optionType = optionTypeById.get(productOptionTypeRequest.getOptionTypeId());
            products.addOptionType(new ProductOptionTypes(optionType, productOptionTypeRequest.getPriority(), true));
        }

        Map<Long, OptionValues> optionValueById = optionTypes.stream()
                .flatMap(ot -> ot.getOptionValues().stream())
                .collect(Collectors.toMap(OptionValues::getId, Function.identity()));

        for(ProductVariantRequest variantRequest :request.getProductVariants()){
            ProductVariants productVariants =
                    new ProductVariants(variantRequest.getSku(), variantRequest.getPrice(), variantRequest.getStockQuantity(), variantRequest.getDiscountRate());

            for(VariantOptionValueRequest valueRequest : variantRequest.getVariantOption()){
                OptionValues optionValues = optionValueById.get(valueRequest.getOptionValueId());
                productVariants.addProductVariantOption(new ProductVariantOptions(optionValues));
            }
            products.addVariant(productVariants);
        }

        Products saved = productsRepository.save(products);
        return new ProductResponse(saved);
    }

    private List<OptionTypes> findOptionTypes(ProductRequest request){
        List<Long> optionTypeIds = request.getProductOptionTypes()
                .stream().map(ProductOptionTypeRequest::getOptionTypeId).toList();
        return findOptionTypeByIdInOrThrow(optionTypeIds);
    }

    private void validateOptionValueCardinality(ProductRequest request, Map<Long, Set<Long>> optionTypeToValueIds){
        for(ProductVariantRequest variantRequest : request.getProductVariants()){
            for(VariantOptionValueRequest v : variantRequest.getVariantOption()){
                Set<Long> allowedValueIds = optionTypeToValueIds.get(v.getOptionTypeId());
                if(!allowedValueIds.contains(v.getOptionValueId())){
                    throw new BadRequestException(ms.getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
                }
            }
        }
    }

    private void validateDuplicateSkus(ProductRequest request){
        List<String> skus = request.getProductVariants().stream().map(ProductVariantRequest::getSku).toList();
        ensureSkusDoNotExist(skus);
    }

    private List<OptionTypes> findOptionTypeByIdInOrThrow(List<Long> optionTypeIds){
        List<OptionTypes> result = optionTypeRepository.findByIdIn(optionTypeIds);
        if(optionTypeIds.size() != result.size()){
            throw new NotFoundException(ms.getMessage(OPTION_TYPE_NOT_FOUND));
        }
        return result;
    }

    private void ensureSkusDoNotExist(Collection<String> skus){
        boolean isConflict = productVariantsRepository.existsBySkuIn(skus);
        if(isConflict){
            throw new DuplicateResourceException(ms.getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
        }
    }

    private Categories findCategoryByIdOrThrow(Long categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CATEGORY_NOT_FOUND)));
    }

    public ProductUpdateResponse updateBasicInfoById(Long productId, UpdateProductBasicRequest request) {
        return null;
    }

    public void deleteProductById(Long productId) {

    }


    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        return null;
    }


    public ProductResponse getProductById(Long productId) {
        return null;
    }


    public PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        return null;
    }


    public PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId) {
        return null;
    }



    public List<ImageResponse> addImages(Long productId, AddImageRequest request) {
        return List.of();
    }


    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request) {
        return null;
    }

}
