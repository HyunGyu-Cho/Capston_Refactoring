package com.example.smart_healthcare.inbody.cache;

import com.example.smart_healthcare.inbody.dto.response.InbodyListResponse;
import com.example.smart_healthcare.inbody.dto.response.InbodySummaryItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class InbodyLatestCacheIntegrationTest {

    @Autowired
    private InbodyLatestCache cache;

    @Test
    void putGetEvict_worksWithRedis() {
        Long memberId = 9999L;
        InbodyListResponse payload = new InbodyListResponse(
                List.of(new InbodySummaryItem(1L, LocalDateTime.of(2026, 3, 15, 10, 0), new BigDecimal("70.00"), new BigDecimal("22.00"), new BigDecimal("15.00"))),
                0,
                5,
                1,
                1
        );

        cache.putLatestFive(memberId, payload);

        InbodyListResponse loaded = cache.getLatestFive(memberId).orElseThrow();
        assertThat(loaded.items()).hasSize(1);
        assertThat(loaded.items().get(0).inbodyId()).isEqualTo(1L);

        cache.evict(memberId);
        assertThat(cache.getLatestFive(memberId)).isEmpty();
    }
}
