package com.example.product_service.service;

import com.example.product_service.dto.KafkaDeletedProduct;
import com.example.product_service.dto.KafkaOrderItemDto;
import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.ProductRequestIdsDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.InsufficientStockException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
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
    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional
    public ProductResponseDto saveProduct(ProductRequestDto productRequestDto) {
        Long categoryId = productRequestDto.getCategoryId();
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Not Found Category"));
        Products products = new Products(
                productRequestDto.getName(),
                productRequestDto.getDescription(),
                productRequestDto.getPrice(),
                productRequestDto.getStockQuantity(),
                category
        );
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
        Products product = productsRepository.findById(productId)
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

        List<Products> findProducts = productsRepository.findAllByIdIn(ids);

        Set<Long> findProductsId = findProducts.stream()
                .map(Products::getId)
                .collect(Collectors.toSet());

        List<Long> foundIds = ids.stream().filter(id -> !findProductsId.contains(id)).toList();

        if(!foundIds.isEmpty()){
            throw new NotFoundException("Not Found product by id:" + foundIds);
        }

        return findProducts.stream().map(CompactProductResponseDto::new).toList();
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
}
