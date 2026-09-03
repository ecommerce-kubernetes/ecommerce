package com.example.product_service.support;

import com.example.product_service.category.adapter.in.web.CategoryController;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.option.adapter.in.web.OptionController;
import com.example.product_service.option.application.service.OptionService;
import com.example.product_service.product.adapter.in.web.InternalProductController;
import com.example.product_service.product.adapter.in.web.ProductController;
import com.example.product_service.product.application.service.ProductService;
import com.example.product_service.product.application.service.VariantService;
import com.example.product_service.support.fixture.FixtureMonkeyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {CategoryController.class, OptionController.class,
        ProductController.class, InternalProductController.class, DummyController.class})
public abstract class ControllerTestSupport {
    protected final FixtureMonkey fixtureMonkey = FixtureMonkeyFactory.get;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected CategoryService categoryService;
    @MockitoBean
    protected OptionService optionService;
    @MockitoBean
    protected ProductService productService;
    @MockitoBean
    protected VariantService variantService;
}
