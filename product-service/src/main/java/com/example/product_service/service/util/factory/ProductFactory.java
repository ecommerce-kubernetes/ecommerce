package com.example.product_service.service.util.factory;

import com.example.product_service.entity.*;
import com.example.product_service.service.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductFactory {


    public Products createProducts(ProductCreationCommand command, ProductCreationData data){
        Products product = createBasicInfoProduct(command, data);
        mappingImages(command, product);
        mappingProductOptionTypes(command, data, product);
        mappingProductVariants(command, data, product);
        return product;
    }


    public ProductVariants createProductVariant(ProductVariantCommand command, ProductVariantCreationData data){
        ProductVariants productVariant = createBasicInfoProductVariant(command);
        List<ProductVariantOptions> productVariantOptions = buildVariantOptions(command.getVariantOptionValues(), data.getOptionValueById());
        productVariant.addProductVariantOptions(productVariantOptions);
        return productVariant;
    }


    private Products createBasicInfoProduct(ProductCreationCommand command, ProductCreationData data){
        return new Products(command.getName(), command.getDescription(), data.getCategory());
    }

    private ProductVariants createBasicInfoProductVariant(ProductVariantCommand command){
        return new ProductVariants(command.getSku(), command.getPrice(), command.getStockQuantity(), command.getDiscountRate());
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

    private void mappingImages(ProductCreationCommand command, Products product){
        List<ProductImages> productImages = command.getImageUrls().stream().map(ProductImages::new).toList();
        product.addImages(productImages);
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
            List<ProductVariantOptions> opts = buildVariantOptions(variantCommand.getVariantOptionValues(), optionValueById);
            productVariant.addProductVariantOptions(opts);
            saveProductVariantList.add(productVariant);
        }
        product.addVariants(saveProductVariantList);
    }

    private List<ProductVariantOptions> buildVariantOptions(List<VariantOptionValueRef> ref,
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
