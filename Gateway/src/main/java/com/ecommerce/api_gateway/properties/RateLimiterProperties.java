package com.ecommerce.api_gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.ratelimiter")
public class RateLimiterProperties {
    private int replenishRate = 1;
    private int burstCapacity = 2;
    private int requestedTokens = 1;

    public int getReplenishRate() { return replenishRate; }
    public void setReplenishRate(int replenishRate) { this.replenishRate = replenishRate; }

    public int getBurstCapacity() { return burstCapacity; }
    public void setBurstCapacity(int burstCapacity) { this.burstCapacity = burstCapacity; }

    public int getRequestedTokens() { return requestedTokens; }
    public void setRequestedTokens(int requestedTokens) { this.requestedTokens = requestedTokens; }
}
