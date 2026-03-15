package com.example.smart_healthcare.inbody.dto.response;

import com.example.smart_healthcare.member.domain.enums.Gender;

import java.time.LocalDate;

public record InbodyMemberSummary(
        Long memberId,
        Gender gender,
        LocalDate birthDate,
        Integer ageAtMeasurement
) {
}
