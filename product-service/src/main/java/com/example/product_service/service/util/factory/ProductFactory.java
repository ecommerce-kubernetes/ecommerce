package com.example.product_service.service.util.factory;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.*;
import com.example.product_service.service.dto.*;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;
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

    public Products createProducts(ProductCreationCommand command, ProductCreationData data){
        Products product = createBasicInfoProduct(command, data);
        mappingImages(command, product);
        mappingProductOptionTypes(command, data, product);
        mappingProductVariants(command, data, product);
        return product;
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

    private Products createBasicInfoProduct(ProductCreationCommand command, ProductCreationData data){
        return new Products(command.getName(), command.getDescription(), data.getCategory());
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

    private void mappingProductOptionTypes(ProductCreationCommand command, ProductCreationData data, Products products){
        Map<Long, OptionTypes> optionTypeById = data.getOptionTypeById();
        List<ProductOptionTypeCommand> optionTypeCommands = command.getOptionTypeCommands();
        List<ProductOptionTypes> saveProductOptionTypeList = new ArrayList<>();
        for(ProductOptionTypeCommand optionTypeCommand : optionTypeCommands){
            OptionTypes optionType = optionTypeById.get(optionTypeCommand.getOptionTypeId());
            ProductOptionTypes productOptionType = new ProductOptionTypes(optionType, optionTypeCommand.getPriority(), optionTypeCommand.isActivate());
            saveProductOptionTypeList.add(productOptionType);
        }

        products.addOptionTypes(saveProductOptionTypeList);
    }

    private void mappingImages(ProductRequest request, Products product){
        List<String> urls = request.getImages();
        List<ProductImages> productImages = urls.stream().map(ProductImages::new).toList();
        product.addImages(productImages);
    }

    private void mappingImages(ProductCreationCommand command, Products product){
        List<ProductImages> productImages = command.getImageUrls().stream().map(ProductImages::new).toList();
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

    private void mappingProductVariants(ProductCreationCommand command, ProductCreationData data, Products product){
        List<ProductVariants> saveProductVariantList = new ArrayList<>();
        Map<Long, OptionValues> optionValueById = data.getOptionValueById();
        for(ProductVariantCommand variantCommand : command.getVariantCommands()){
            ProductVariants productVariant = new ProductVariants(
                    variantCommand.getSku(),
                    variantCommand.getPrice(),
                    variantCommand.getStockQuantity(),
                    variantCommand.getDiscountRate()
            );
            List<ProductVariantOptions> opts = buildVariantOptionsNew(variantCommand.getVariantOptionValues(), optionValueById);
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
            OptionValues optionValue = optionValueById.get(optionValueId);
            ProductVariantOptions productVariantOption = new ProductVariantOptions(optionValue);
            saveProductVariantOptions.add(productVariantOption);
        }

        return saveProductVariantOptions;
    }

    private List<ProductVariantOptions> buildVariantOptionsNew(List<VariantOptionValueRef> ref,
                                                            Map<Long, OptionValues> optionValueById){
        List<ProductVariantOptions> saveProductVariantOptions = new ArrayList<>();
        for(VariantOptionValueRef variantOptionValueRef : ref){
            Long optionValueId = variantOptionValueRef.getOptionValueId();
            OptionValues optionValue = optionValueById.get(optionValueId);
            ProductVariantOptions productVariantOption = new ProductVariantOptions(optionValue);
            saveProductVariantOptions.add(productVariantOption);
        }

        return saveProductVariantOptions;
    }
}
