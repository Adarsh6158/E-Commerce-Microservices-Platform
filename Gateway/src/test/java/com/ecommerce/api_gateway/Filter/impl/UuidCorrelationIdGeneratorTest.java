package com.ecommerce.api_gateway.Filter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidCorrelationIdGeneratorTest {

    private final UuidCorrelationIdGenerator generator = new UuidCorrelationIdGenerator();

    @Test
    @DisplayName("Should generate valid UUID string")
    void generate_shouldReturnValidUuid() {
        String id = generator.generate();
        assertNotNull(id);
        assertDoesNotThrow(() -> UUID.fromString(id));
    }

    @Test
    @DisplayName("Should not generate null")
    void generate_shouldNotBeNull() {
        assertNotNull(generator.generate());
    }

    @Test
    @DisplayName("Should not generate empty string")
    void generate_shouldNotBeEmpty() {
        assertFalse(generator.generate().isEmpty());
    }

    @Test
    @DisplayName("Should be exactly 36 characters long")
    void generate_shouldHaveCorrectLength() {
        assertEquals(36, generator.generate().length());
    }

    @Test
    @DisplayName("Should generate version 4 UUID")
    void generate_shouldBeVersion4() {
        String id = generator.generate();
        assertEquals(4, UUID.fromString(id).version());
    }

    @Test
    @DisplayName("Should generate unique IDs across multiple calls")
    void generate_shouldBeUnique() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(generator.generate());
        }
        assertEquals(1000, ids.size());
    }

    @Test
    @DisplayName("Should generate lowercase string")
    void generate_shouldBeLowercase() {
        String id = generator.generate();
        assertEquals(id.toLowerCase(), id);
    }

    @Test
    @DisplayName("Should not contain spaces")
    void generate_shouldNotContainSpaces() {
        assertFalse(generator.generate().contains(" "));
    }

}
