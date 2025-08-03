package com.example.product_service.controller.util.specification.config;

import com.example.product_service.controller.util.specification.annotation.AdminApi;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OperationCustomizer adminHeaderCustomizer(){
        return (operation, handlerMethod) -> {
            if(handlerMethod.hasMethodAnnotation(AdminApi.class)){
                operation.addParametersItem(
                        new Parameter()
                                .in("header")
                                .name("X-User-Role")
                                .required(true)
                                .description("관리자 권한 헤더 (ROLE_ADMIN)")
                                .schema(new StringSchema()
                                        ._enum(java.util.List.of("ROLE_USER", "ROLE_ADMIN")))
                );
            }

            return operation;
        };
    }
}
