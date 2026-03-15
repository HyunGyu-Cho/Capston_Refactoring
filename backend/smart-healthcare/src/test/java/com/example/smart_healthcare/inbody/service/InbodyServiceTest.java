package com.example.smart_healthcare.inbody.service;

import com.example.smart_healthcare.inbody.cache.InbodyLatestCache;
import com.example.smart_healthcare.inbody.dto.request.InbodyInputRequest;
import com.example.smart_healthcare.inbody.dto.response.InbodyInputResponse;
import com.example.smart_healthcare.inbody.id.SnowflakeIdGenerator;
import com.example.smart_healthcare.inbody.infrastructure.InbodyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.smart_healthcare.member.domain.Member;
import com.example.smart_healthcare.member.domain.enums.Gender;
import com.example.smart_healthcare.member.infrastructure.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InbodyServiceTest {

    @Mock
    private InbodyRepository inbodyRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Mock
    private StringRedisTemplate redisTemplate;

    private InbodyLatestCache inbodyLatestCache;

    @InjectMocks
    private InbodyService inbodyService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member("user@test.com", "hashed", "nick", Gender.MALE, LocalDate.of(2000, 1, 1));
        inbodyLatestCache = new InbodyLatestCache(new ObjectMapper(), redisTemplate, new SimpleMeterRegistry());
        inbodyService = new InbodyService(inbodyRepository, memberRepository, snowflakeIdGenerator, inbodyLatestCache);
    }

    @Test
    void create_calculatesAndStoresDerivedValues() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(snowflakeIdGenerator.nextId()).thenReturn(1001L);
        when(inbodyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InbodyInputRequest request = new InbodyInputRequest(
                LocalDateTime.of(2026, 3, 15, 9, 30),
                new BigDecimal("173"),
                new BigDecimal("73"),
                new BigDecimal("12.5"),
                new BigDecimal("33.4"),
                new BigDecimal("46.8"),
                new BigDecimal("0.81"),
                5
        );

        InbodyInputResponse response = inbodyService.create(1L, request);

        assertThat(response.inbodyId()).isEqualTo(1001L);
        assertThat(response.metrics().bmi()).isEqualByComparingTo("24.39");
        assertThat(response.metrics().bodyFatPercent()).isEqualByComparingTo("17.12");
        assertThat(response.member().ageAtMeasurement()).isEqualTo(26);
    }

    @Test
    void getMyList_rejectsUnsupportedSize() {
        assertThatThrownBy(() -> inbodyService.getMyList(1L, 0, 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size must be one of");
    }
}
