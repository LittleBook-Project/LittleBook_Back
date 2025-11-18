package com.littlebook.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI adminServiceOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Admin Service API")
                        .description("API d'administration et de statistiques pour LittleBook")
                        .version("v1.0")
                        .contact(new Contact().name("LittleBook API Team").email("devs@littlebook.local"))
                );
    }
}
