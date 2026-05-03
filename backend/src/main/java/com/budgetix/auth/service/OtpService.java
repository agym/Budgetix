package com.budgetix.auth.service;

import com.budgetix.common.enums.OtpType;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.user.entity.OtpCode;
import com.budgetix.user.entity.User;
import com.budgetix.user.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generateAndSave(User user, OtpType type, int expiryMinutes) {
        otpCodeRepository.invalidateAll(user.getId(), type);

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        OtpCode otp = OtpCode.builder()
            .user(user)
            .code(code)
            .type(type)
            .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
            .build();

        otpCodeRepository.save(otp);
        return code;
    }

    @Transactional
    public void validate(User user, OtpType type, String code) {
        OtpCode otp = otpCodeRepository.findLatestValid(user.getId(), type)
            .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));

        if (otp.isExpired() || !otp.getCode().equals(code)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }
}
