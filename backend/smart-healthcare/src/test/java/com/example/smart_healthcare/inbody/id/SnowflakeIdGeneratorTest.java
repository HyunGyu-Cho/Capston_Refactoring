package com.example.smart_healthcare.inbody.id;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SnowflakeIdGeneratorTest {

    @Test
    void nextId_generatesUniqueIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Set<Long> ids = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            ids.add(generator.nextId());
        }

        assertThat(ids).hasSize(10000);
    }
}
