package com.example.product_service.service.util.factory;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.*;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductVariantCreationData;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProductFactory {

    public Products createProducts(ProductRequest request, ProductCreationData data){
        Products products = createBasicInfoProduct(request.getName(), request.getDescription(), data.getCategory());
        mappingImages(request, products);
        mappingProductOptionTypes(request, data, products);
        mappingProductVariants(request, data, products);

        return products;
    }

    public ProductVariants createProductVariant(ProductVariantRequest request, ProductVariantCreationData data){
        ProductVariants productVariant = new ProductVariants(request.getSku(), request.getPrice(), request.getStockQuantity(), request.getDiscountRate());
        List<ProductVariantOptions> opts = buildVariantOptions(request.getVariantOption(), data.getOptionValueById());
        productVariant.addProductVariantOptions(opts);
        return productVariant;
    }

    private Products createBasicInfoProduct(String name, String description, Categories category){
        return new Products(name, description, category);
    }

    private void mappingProductOptionTypes(ProductRequest request, ProductCreationData data, Products product){
        Map<Long, OptionTypes> optionTypeById = data.getOptionTypeById();
        List<ProductOptionTypeRequest> productOptionTypes = request.getProductOptionTypes();
        List<ProductOptionTypes> saveProductOptionTypeList = new ArrayList<>();
        for (ProductOptionTypeRequest potRequest : productOptionTypes) {
            OptionTypes optionType = optionTypeById.get(potRequest.getOptionTypeId());
            ProductOptionTypes productOptionType = new ProductOptionTypes(optionType, potRequest.getPriority(), true);
            saveProductOptionTypeList.add(productOptionType);
        }

        product.addOptionTypes(saveProductOptionTypeList);
    }

    private void mappingImages(ProductRequest request, Products product){
        List<String> urls = request.getImages().stream().map(ImageRequest::getUrl).toList();
        List<ProductImages> productImages = urls.stream().map(ProductImages::new).toList();
        product.addImages(productImages);
    }

    private void mappingProductVariants(ProductRequest request, ProductCreationData data, Products product){
        List<ProductVariants> saveProductVariantList = new ArrayList<>();
        Map<Long, OptionValues> optionValueById = data.getOptionValueById();
        for (ProductVariantRequest variantRequest : request.getProductVariants()) {
            ProductVariants productVariant = new ProductVariants(
                    variantRequest.getSku(),
                    variantRequest.getPrice(),
                    variantRequest.getStockQuantity(),
                    variantRequest.getDiscountRate()
            );
            List<ProductVariantOptions> opts = buildVariantOptions(variantRequest.getVariantOption(), optionValueById);
            productVariant.addProductVariantOptions(opts);
            saveProductVariantList.add(productVariant);
        }
        product.addVariants(saveProductVariantList);
    }

    private List<ProductVariantOptions> buildVariantOptions(List<VariantOptionValueRequest> variantOptionValueRequests,
                                                            Map<Long, OptionValues> optionValueById){
        List<ProductVariantOptions> saveProductVariantOptions = new ArrayList<>();
        for (VariantOptionValueRequest optionValueRequest : variantOptionValueRequests) {

            Long optionValueId = optionValueRequest.getOptionValueId();
            log.info("요청 optionValueId {}", optionValueId);
            OptionValues optionValue = optionValueById.get(optionValueId);
            if(optionValue == null){
                log.info("null 임");
            }
            ProductVariantOptions productVariantOption = new ProductVariantOptions(optionValue);
            saveProductVariantOptions.add(productVariantOption);
        }

        return saveProductVariantOptions;
    }
}
