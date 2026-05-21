package com.ecommerce.api_gateway.Config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorsConfigTest {

    @Test
    @DisplayName("Should create CorsWebFilter bean correctly")
    void corsWebFilter_whenCalled_shouldReturnNonNullFilter() {
        CorsConfig corsConfig = new CorsConfig();
        CorsWebFilter filter = corsConfig.corsWebFilter();
        
        assertNotNull(filter, "CorsWebFilter should not be null");
    }
}
