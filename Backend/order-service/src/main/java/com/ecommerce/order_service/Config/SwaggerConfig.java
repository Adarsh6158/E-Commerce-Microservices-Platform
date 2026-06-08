package com.ecommerce.order_service.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("E-Commerce order management, saga orchestration, invoice generation, and purchase verification")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Adarsh")
                                .email("adarsh@shopflux.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8087").description("Local"),
                        new Server().url("http://order-service:8087").description("Docker")));
    }
}
