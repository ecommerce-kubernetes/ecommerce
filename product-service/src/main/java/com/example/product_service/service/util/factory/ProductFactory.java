package com.example.product_service.service.util.factory;

import com.example.product_service.entity.*;
import com.example.product_service.service.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductFactory {


    public Product createProducts(ProductCreationCommand command, ProductCreationData data){
        Product product = createBasicInfoProduct(command, data);
        mappingImages(command, product);
        mappingProductOptionTypes(command, data, product);
        mappingProductVariants(command, data, product);
        return product;
    }


    public ProductVariant createProductVariant(ProductVariantCommand command, ProductVariantCreationData data){
        ProductVariant productVariant = createBasicInfoProductVariant(command);
        List<ProductVariantOption> productVariantOptions = buildVariantOptions(command.getVariantOptionValues(), data.getOptionValueById());
        productVariant.addProductVariantOptions(productVariantOptions);
        return productVariant;
    }


    private Product createBasicInfoProduct(ProductCreationCommand command, ProductCreationData data){
        return null;
//        return new Product(command.getName(), command.getDescription(), data.getCategory());
    }

    private ProductVariant createBasicInfoProductVariant(ProductVariantCommand command){
        return new ProductVariant(command.getSku(), command.getPrice(), command.getStockQuantity(), command.getDiscountRate());
    }

    private void mappingProductOptionTypes(ProductCreationCommand command, ProductCreationData data, Product product){
        Map<Long, OptionType> optionTypeById = data.getOptionTypeById();
        List<ProductOptionTypeCommand> optionTypeCommands = command.getOptionTypeCommands();
        List<ProductOptionType> saveProductOptionTypeList = new ArrayList<>();
        for(ProductOptionTypeCommand optionTypeCommand : optionTypeCommands){
            OptionType optionType = optionTypeById.get(optionTypeCommand.getOptionTypeId());
            ProductOptionType productOptionType = new ProductOptionType(optionType, optionTypeCommand.getPriority(), optionTypeCommand.isActivate());
            saveProductOptionTypeList.add(productOptionType);
        }

        product.addOptionTypes(saveProductOptionTypeList);
    }

    private void mappingImages(ProductCreationCommand command, Product product){
        List<ProductImage> productImages = command.getImageUrls().stream().map(ProductImage::new).toList();
        product.addImages(productImages);
    }

    private void mappingProductVariants(ProductCreationCommand command, ProductCreationData data, Product product){
        List<ProductVariant> saveProductVariantList = new ArrayList<>();
        Map<Long, OptionValue> optionValueById = data.getOptionValueById();
        for(ProductVariantCommand variantCommand : command.getVariantCommands()){
            ProductVariant productVariant = new ProductVariant(
                    variantCommand.getSku(),
                    variantCommand.getPrice(),
                    variantCommand.getStockQuantity(),
                    variantCommand.getDiscountRate()
            );
            List<ProductVariantOption> opts = buildVariantOptions(variantCommand.getVariantOptionValues(), optionValueById);
            productVariant.addProductVariantOptions(opts);
            saveProductVariantList.add(productVariant);
        }
        product.addVariants(saveProductVariantList);
    }

    private List<ProductVariantOption> buildVariantOptions(List<VariantOptionValueRef> ref,
                                                           Map<Long, OptionValue> optionValueById){
        List<ProductVariantOption> saveProductVariantOptions = new ArrayList<>();
        for(VariantOptionValueRef variantOptionValueRef : ref){
            Long optionValueId = variantOptionValueRef.getOptionValueId();
            OptionValue optionValue = optionValueById.get(optionValueId);
            ProductVariantOption productVariantOption = new ProductVariantOption(optionValue);
            saveProductVariantOptions.add(productVariantOption);
        }

        return saveProductVariantOptions;
    }
}
