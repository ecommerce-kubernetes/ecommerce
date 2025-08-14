package com.example.product_service.service.util;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.repository.ProductVariantsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductRequestValidatorTest {
    @Mock
    ProductVariantsRepository productVariantsRepository;

    @Mock
    MessageSourceUtil ms;

    @InjectMocks
    ProductRequestValidator validator;
}