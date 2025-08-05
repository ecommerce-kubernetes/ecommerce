package com.example.product_service.controller;

import com.example.product_service.controller.util.ControllerResponseValidator;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValuesResponseDto;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.OptionValueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionValueController.class)
@Import(ControllerResponseValidator.class)
class OptionValueControllerTest {
}