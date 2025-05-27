package com.example.product_service.controller;

import com.example.product_service.controller.util.SortFieldValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(ProductController.class)
@Import(SortFieldValidator.class)
@Slf4j
class ProductControllerTest {


}