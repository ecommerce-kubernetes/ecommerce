package com.example.userservice.support;

import com.example.userservice.auth.adapter.in.web.AuthController;
import com.example.userservice.auth.application.service.AuthService;
import com.example.userservice.user.adapter.in.web.UserController;
import com.example.userservice.user.application.service.UserCommandService;
import com.example.userservice.user.application.service.UserQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {UserController.class, AuthController.class, DummyController.class})
public class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected UserCommandService userCommandService;
    @MockitoBean
    protected UserQueryService userQueryService;
    @MockitoBean
    protected AuthService authService;
}
