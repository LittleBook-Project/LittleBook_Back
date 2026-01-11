package com.littlebook.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificaionServiceOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("notification Service API")
                        .version("v1")
                        .description("API pour gérer les livres (notificaions-service)"));
    }
}
