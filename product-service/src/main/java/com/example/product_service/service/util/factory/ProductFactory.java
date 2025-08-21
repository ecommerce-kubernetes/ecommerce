package com.example.product_service.service.util.factory;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.*;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductVariantCreationData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
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
        mappingVariantOption(request.getVariantOption(), data, productVariant);
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
        int sortOrder = product.getImages().size();
        List<ProductImages> images = new ArrayList<>();
        for(ImageRequest imageRequest : request.getImages()){
            images.add(new ProductImages(imageRequest.getUrl(), sortOrder++));
        }
        product.addImages(images);
    }

    private void mappingProductVariants(ProductRequest request, ProductCreationData data, Products product){
        List<ProductVariantRequest> productVariantRequests = request.getProductVariants();
        List<ProductVariants> saveProductVariantList = new ArrayList<>();
        for (ProductVariantRequest variantRequest : productVariantRequests) {
            ProductVariants productVariant = new ProductVariants(variantRequest.getSku(), variantRequest.getPrice(),
                    variantRequest.getStockQuantity(), variantRequest.getDiscountRate());
            mappingVariantOption(variantRequest.getVariantOption(), data, productVariant);
            saveProductVariantList.add(productVariant);
        }
        product.addVariants(saveProductVariantList);
    }

    private void mappingVariantOption(List<VariantOptionValueRequest> variantOptionValueRequests, ProductCreationData data,
                                      ProductVariants productVariants){
        List<ProductVariantOptions> saveProductVariantOptions = new ArrayList<>();
        for (VariantOptionValueRequest optionValueRequest : variantOptionValueRequests) {
            Map<Long, OptionValues> optionValueById = data.getOptionValueById();
            Long optionValueId = optionValueRequest.getOptionValueId();
            OptionValues optionValue = optionValueById.get(optionValueId);
            ProductVariantOptions productVariantOption = new ProductVariantOptions(optionValue);
            saveProductVariantOptions.add(productVariantOption);
        }

        productVariants.addProductVariantOptions(saveProductVariantOptions);
    }

    private void mappingVariantOption(List<VariantOptionValueRequest> variantOptionValueRequests, ProductVariantCreationData data,
                                      ProductVariants productVariant){
        List<ProductVariantOptions> saveProductVariantOptions = new ArrayList<>();
        for (VariantOptionValueRequest optionValueRequest : variantOptionValueRequests) {
            Map<Long, OptionValues> optionValueById = data.getOptionValueById();
            Long optionValueId = optionValueRequest.getOptionValueId();
            OptionValues optionValue = optionValueById.get(optionValueId);
            ProductVariantOptions productVariantOption = new ProductVariantOptions(optionValue);
            saveProductVariantOptions.add(productVariantOption);
        }

        productVariant.addProductVariantOptions(saveProductVariantOptions);
    }
}
