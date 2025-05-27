package com.example.product_service.service;

import com.example.product_service.entity.Categories;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.ProductImagesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.client.ImageClientService;
import com.example.product_service.service.kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Slf4j
class ProductServiceImplTest {

    @Autowired
    ProductService productService;
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    CategoriesRepository categoriesRepository;

    @MockitoBean
    KafkaProducer kafkaProducer;

    @Autowired
    ProductImagesRepository productImagesRepository;

    @MockitoBean
    ImageClientService imageClientService;

    private Categories food;
    private Categories electronicDevices;


}