package com.example.smart_healthcare.inbody.service;

import com.example.smart_healthcare.common.error.AppException;
import com.example.smart_healthcare.inbody.cache.InbodyLatestCache;
import com.example.smart_healthcare.inbody.domain.Inbody;
import com.example.smart_healthcare.inbody.dto.request.InbodyInputRequest;
import com.example.smart_healthcare.inbody.dto.response.InbodyDetailResponse;
import com.example.smart_healthcare.inbody.dto.response.InbodyInputResponse;
import com.example.smart_healthcare.inbody.dto.response.InbodyListResponse;
import com.example.smart_healthcare.inbody.dto.response.InbodyMemberSummary;
import com.example.smart_healthcare.inbody.dto.response.InbodyMetricsSummary;
import com.example.smart_healthcare.inbody.dto.response.InbodySummaryItem;
import com.example.smart_healthcare.inbody.error.InbodyErrorCode;
import com.example.smart_healthcare.inbody.id.SnowflakeIdGenerator;
import com.example.smart_healthcare.inbody.infrastructure.InbodyRepository;
import com.example.smart_healthcare.member.domain.Member;
import com.example.smart_healthcare.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InbodyService {

    // 계산식/반올림 기준이 바뀔 때 응답/분석 기준 추적을 위해 버전값을 남긴다.
    private static final String CALCULATION_VERSION = "v1";
    // 목록 조회에서 허용하는 페이지 크기를 고정해 과도한 요청으로 인한 부하를 방지한다.
    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(5, 10, 20, 50, 100);

    private final InbodyRepository inbodyRepository;
    private final MemberRepository memberRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final InbodyLatestCache inbodyLatestCache;

    @Transactional
    public InbodyInputResponse create(Long memberId, InbodyInputRequest request) {
        // 인증 사용자의 member_id 기준으로만 저장한다. (클라이언트 입력 member_id 미사용)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(InbodyErrorCode.INBODY_403_001));

        // 원본 측정값으로 핵심 파생값을 계산해 DB에 함께 저장한다.
        BigDecimal bmi = calculateBmi(request.heightCm(), request.weightKg());
        BigDecimal bodyFatPercent = calculateBodyFatPercent(request.bodyFatMassKg(), request.weightKg());
        Integer ageAtMeasurement = calculateAgeAtMeasurement(member.getBirthDate(), request.measuredAt());

        Inbody inbody = Inbody.builder()
                .id(snowflakeIdGenerator.nextId())
                .member(member)
                .measuredAt(request.measuredAt())
                .heightCm(scale(request.heightCm()))
                .weightKg(scale(request.weightKg()))
                .bodyFatMassKg(scaleNullable(request.bodyFatMassKg()))
                .skeletalMuscleMassKg(scaleNullable(request.skeletalMuscleMassKg()))
                .bodyWaterL(scaleNullable(request.bodyWaterL()))
                .waistHipRatio(scaleNullable(request.waistHipRatio()))
                .visceralFatLevel(request.visceralFatLevel())
                .bmi(bmi)
                .bodyFatPercent(bodyFatPercent)
                .ageAtMeasurement(ageAtMeasurement)
                .calculationVersion(CALCULATION_VERSION)
                .build();

        try {
            Inbody saved = inbodyRepository.save(inbody);
            // 저장 직후 최신 캐시를 무효화해 stale 데이터 노출을 막는다.
            inbodyLatestCache.evict(memberId);
            return toInputResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // UNIQUE(member_id, measured_at) 제약 충돌 시 중복 측정 시각 오류로 변환한다.
            throw new AppException(InbodyErrorCode.INBODY_409_001);
        }
    }

    @Transactional(readOnly = true)
    public InbodyDetailResponse getDetail(Long memberId, Long inbodyId) {
        Inbody inbody = inbodyRepository.findByIdAndMember_Id(inbodyId, memberId)
                .orElseThrow(() -> new AppException(InbodyErrorCode.INBODY_404_001));
        return toDetailResponse(inbody);
    }

    @Transactional(readOnly = true)
    public InbodyListResponse getMyList(Long memberId, int page, int size) {
        validatePageSize(size);
        // 가장 빈번한 "최신 5건 첫 페이지"는 캐시 우선 조회한다.
        if (page == 0 && size == 5) {
            return inbodyLatestCache.getLatestFive(memberId)
                    .orElseGet(() -> {
                        InbodyListResponse response = queryList(memberId, page, size);
                        inbodyLatestCache.putLatestFive(memberId, response);
                        return response;
                    });
        }
        return queryList(memberId, page, size);
    }

    private InbodyListResponse queryList(Long memberId, int page, int size) {
        // measured_at 1차, inbody_id 2차 정렬로 최신순/동시간 tie-break를 보장한다.
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("measuredAt"), Sort.Order.desc("id")));
        Page<Inbody> result = inbodyRepository.findByMember_IdOrderByMeasuredAtDescIdDesc(memberId, pageable);

        List<InbodySummaryItem> items = result.getContent().stream()
                .map(this::toSummary)
                .toList();

        return new InbodyListResponse(items, page, size, result.getTotalElements(), result.getTotalPages());
    }

    private void validatePageSize(int size) {
        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            throw new IllegalArgumentException("size must be one of 5,10,20,50,100");
        }
    }

    private BigDecimal calculateBmi(BigDecimal heightCm, BigDecimal weightKg) {
        // BMI = 체중(kg) / 신장(m)^2, 소수점 2자리 반올림
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        BigDecimal squared = heightM.multiply(heightM);
        return weightKg.divide(squared, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBodyFatPercent(BigDecimal bodyFatMassKg, BigDecimal weightKg) {
        // 체지방량이 없으면 체지방률도 계산하지 않는다.
        if (bodyFatMassKg == null) {
            return null;
        }
        // 0 나눗셈 방지
        if (weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return bodyFatMassKg.multiply(BigDecimal.valueOf(100))
                .divide(weightKg, 2, RoundingMode.HALF_UP);
    }

    private Integer calculateAgeAtMeasurement(LocalDate birthDate, LocalDateTime measuredAt) {
        // "현재 나이"가 아니라 "측정 시점 나이"를 저장한다.
        return Period.between(birthDate, measuredAt.toLocalDate()).getYears();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleNullable(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private InbodyInputResponse toInputResponse(Inbody inbody) {
        return new InbodyInputResponse(
                inbody.getId(),
                inbody.getMeasuredAt(),
                toMemberSummary(inbody),
                toMetricsSummary(inbody),
                inbody.getCalculationVersion()
        );
    }

    private InbodyDetailResponse toDetailResponse(Inbody inbody) {
        return new InbodyDetailResponse(
                inbody.getId(),
                inbody.getMeasuredAt(),
                toMemberSummary(inbody),
                toMetricsSummary(inbody),
                inbody.getCalculationVersion()
        );
    }

    private InbodySummaryItem toSummary(Inbody inbody) {
        return new InbodySummaryItem(
                inbody.getId(),
                inbody.getMeasuredAt(),
                inbody.getWeightKg(),
                inbody.getBmi(),
                inbody.getBodyFatPercent()
        );
    }

    private InbodyMemberSummary toMemberSummary(Inbody inbody) {
        Member member = inbody.getMember();
        return new InbodyMemberSummary(
                member.getId(),
                member.getGender(),
                member.getBirthDate(),
                inbody.getAgeAtMeasurement()
        );
    }

    private InbodyMetricsSummary toMetricsSummary(Inbody inbody) {
        return new InbodyMetricsSummary(
                inbody.getHeightCm(),
                inbody.getWeightKg(),
                inbody.getBmi(),
                inbody.getBodyFatMassKg(),
                inbody.getBodyFatPercent(),
                inbody.getSkeletalMuscleMassKg(),
                inbody.getBodyWaterL(),
                inbody.getWaistHipRatio(),
                inbody.getVisceralFatLevel()
        );
    }
}
