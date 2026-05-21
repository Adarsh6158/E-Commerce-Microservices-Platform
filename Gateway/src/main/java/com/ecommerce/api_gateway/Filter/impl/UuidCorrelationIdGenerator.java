package com.ecommerce.api_gateway.Filter.impl;

import com.ecommerce.api_gateway.Filter.interfaces.ICorrelationIdGenerator;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UuidCorrelationIdGenerator implements ICorrelationIdGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
