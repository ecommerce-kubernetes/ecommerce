package com.example.product_service.service;

import com.example.product_service.dto.KafkaDeletedProduct;
import com.example.product_service.dto.KafkaOrderItemDto;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.ProductImages;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.InsufficientStockException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.ProductImagesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService{

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;
    private final ProductImagesRepository productImagesRepository;
    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional
    public ProductResponseDto saveProduct(ProductRequestDto productRequestDto) {
        Long categoryId = productRequestDto.getCategoryId();
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));

        List<String> imageUrls = productRequestDto.getProductImageRequestDto().getImageUrls();
        Products products = new Products(
                productRequestDto.getName(),
                productRequestDto.getDescription(),
                productRequestDto.getPrice(),
                productRequestDto.getStockQuantity(),
                category
        );

        for(int i=0; i<imageUrls.size(); i++){
            new ProductImages(products,imageUrls.get(i), i);
        }
        Products save = productsRepository.save(products);
        return new ProductResponseDto(save);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));
        KafkaDeletedProduct kafkaDeletedProduct = new KafkaDeletedProduct(product.getId());
        kafkaProducer.sendMessage("delete_product", kafkaDeletedProduct);
        productsRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponseDto modifyStockQuantity(Long productId, StockQuantityRequestDto stockQuantityRequestDto) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));
        int updateStockQuantity = stockQuantityRequestDto.getUpdateStockQuantity();
        product.setStockQuantity(updateStockQuantity);

        return new ProductResponseDto(product);
    }

    @Override
    public ProductResponseDto getProductDetails(Long productId) {
        Products product = productsRepository.findByIdWithProductImages(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));

        return new ProductResponseDto(product);
    }

    @Override
    public PageDto<ProductResponseDto> getProductList(Pageable pageable, Long categoryId, String name) {
        Page<Products> productsPage = productsRepository.findAllByParameter(name, categoryId, pageable);
        List<ProductResponseDto> content = productsPage.getContent().stream().map(ProductResponseDto::new).toList();

        return new PageDto<>(
                content,
                pageable.getPageNumber(),
                productsPage.getTotalPages(),
                pageable.getPageSize(),
                productsPage.getTotalElements()
        );
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

            int currentStock = products.getStockQuantity();
            int orderQuantity = orderedItem.getQuantity();
            if(currentStock < orderQuantity) {
                throw new InsufficientStockException("Not Enough  stock for product with id: " + products.getId());
            }

            products.decrementQuantity(orderedItem.getQuantity());
        }
    }

    @Override
    @Transactional
    public ProductResponseDto addImage(Long productId, ProductImageRequestDto productImageRequestDto) {

        List<String> imageUrls = productImageRequestDto.getImageUrls();
        Products product = productsRepository.findByIdWithProductImages(productId)
                .orElseThrow(() -> new NotFoundException("Not Found Product"));

        int nextOrder = product.getImages().stream()
                .map(ProductImages::getSortOrder)
                .max(Integer::compareTo)
                .orElse(-1) + 1;

        for (String url : imageUrls) {
            new ProductImages(product, url, nextOrder++);
        }
        productsRepository.save(product);
        return new ProductResponseDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDto imgSwapOrder(Long productId, ImageOrderRequestDto imageOrderRequestDto) {
        ProductImages target = productImagesRepository.findById(imageOrderRequestDto.getImageId())
                .orElseThrow(() -> new NotFoundException("Not Found ProductImage"));

        ProductImages conflict = productImagesRepository.findByProductIdAndSortOrder(productId, imageOrderRequestDto.getSortOrder())
                .orElseThrow(() -> new NotFoundException("Not Found ProductImage"));
        int oldOrder = target.getSortOrder();
        target.setSortOrder(imageOrderRequestDto.getSortOrder());
        conflict.setSortOrder(oldOrder);

        return new ProductResponseDto(target.getProduct());
    }

    @Override
    public void deleteImage(Long productId, Long imageId) {
    }
}
