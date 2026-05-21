package com.ecommerce.api_gateway.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShopFlux API Gateway")
                        .version("1.0.0")
                        .description("ShopFlux E-Commerce Platform Reactive API Gateway. This gateway routes all public and secured API requests to the respective microservices, handles centralized JWT authentication, enforces rate-limiting, and manages circuit-breaker fallbacks.")
                        .contact(new Contact()
                                .name("ShopFlux Development Team")
                                .email("support@shopflux.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Please enter a valid JWT token using the Bearer scheme (e.g. Bearer <token>) in the Authorization header to access protected routes.")));
    }
}
