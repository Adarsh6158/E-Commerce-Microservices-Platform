package com.ecommerce.api_gateway;

import com.ecommerce.api_gateway.properties.JwtProperties;
import com.ecommerce.api_gateway.properties.RateLimiterProperties;
import com.ecommerce.api_gateway.properties.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, RateLimiterProperties.class, SecurityProperties.class})
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
