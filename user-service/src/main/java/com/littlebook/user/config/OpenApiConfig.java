package com.littlebook.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("User Service API")
                        .description("API pour la gestion des utilisateurs (profiles, provisioning depuis OAuth)")
                        .version("v1.0"));
    }
}
