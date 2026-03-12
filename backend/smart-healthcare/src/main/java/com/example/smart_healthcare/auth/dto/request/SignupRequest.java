package com.example.smart_healthcare.auth.dto.request;

import com.example.smart_healthcare.member.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record SignupRequest(
        @Email @NotBlank String email,
        @NotBlank @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,64}$",
                message = "Password must include letters, numbers, and symbols (8-64 chars)")
        String password,
        @NotBlank String nickname,
        @NotNull LocalDate birthDate,
        @NotNull Gender gender
) {
}
