package com.ecommerce.catalog_service.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Catalog Service API")
                        .description("Manages products, categories, reviews, and product images for the e-commerce platform.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("E-Commerce Platform Team")));
    }
}
