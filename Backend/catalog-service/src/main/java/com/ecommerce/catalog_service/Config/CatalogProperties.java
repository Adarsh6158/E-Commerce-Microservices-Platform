package com.ecommerce.catalog_service.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "catalog")
public class CatalogProperties {

    private Cache cache = new Cache();
    private Images images = new Images();
    private OrderService orderService = new OrderService();
    private WebClientConfig webClient = new WebClientConfig();



    public static class Cache {
        private String prefix = "product:";
        private int ttlMinutes = 5;

        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
        public int getTtlMinutes() { return ttlMinutes; }
        public void setTtlMinutes(int ttlMinutes) { this.ttlMinutes = ttlMinutes; }
    }



    public static class Images {
        private String uploadDir = "./uploads/products";
        private String baseUrl = "http://localhost:8082/products/images";
        private long maxFileSizeBytes = 5 * 1024 * 1024;
        private List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

        public String getUploadDir() { return uploadDir; }
        public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
        public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }
        public List<String> getAllowedTypes() { return allowedTypes; }
        public void setAllowedTypes(List<String> allowedTypes) { this.allowedTypes = allowedTypes; }
    }



    public static class OrderService {
        private String host = "localhost";
        private int port = 8087;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }



    public static class WebClientConfig {
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 5000;

        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    }



    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }
    public Images getImages() { return images; }
    public void setImages(Images images) { this.images = images; }
    public OrderService getOrderService() { return orderService; }
    public void setOrderService(OrderService orderService) { this.orderService = orderService; }
    public WebClientConfig getWebClient() { return webClient; }
    public void setWebClient(WebClientConfig webClient) { this.webClient = webClient; }
}
