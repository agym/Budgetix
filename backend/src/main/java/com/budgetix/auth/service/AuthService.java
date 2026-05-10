package com.budgetix.auth.service;

import com.budgetix.auth.dto.*;
import com.budgetix.common.enums.OtpType;
import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.common.util.JwtUtil;
import com.budgetix.user.entity.RefreshToken;
import com.budgetix.user.entity.User;
import com.budgetix.user.entity.UserProfile;
import com.budgetix.user.repository.RefreshTokenRepository;
import com.budgetix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final EmailService emailService;

    @Value("${app.verification.max-resend-attempts:3}")
    private int maxResendAttempts;

    @Value("${app.verification.resend-window-hours:24}")
    private int resendWindowHours;

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserProfile profile = UserProfile.builder().build();
        User user = User.builder()
            .email(req.email().toLowerCase().strip())
            .password(passwordEncoder.encode(req.password()))
            .name(req.name())
            .profile(profile)
            .build();
        profile.setUser(user);

        userRepository.save(user);

        String code = otpService.generateAndSave(user, OtpType.EMAIL_VERIFICATION, 15);
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), code);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
            .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!"LOCAL".equals(user.getProvider())) {
            throw new AppException(ErrorCode.OAUTH_ACCOUNT);
        }

        if (user.getPassword() == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (user.isTwoFactorEnabled()) {
            if (req.totpCode() == null || req.totpCode().isBlank()) {
                throw new AppException(ErrorCode.TWO_FACTOR_REQUIRED);
            }
            otpService.validate(user, OtpType.TWO_FACTOR, req.totpCode());
        }

        return issueTokenPair(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        if (!jwtUtil.isRefreshTokenValid(req.refreshToken())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken stored = refreshTokenRepository.findByToken(req.refreshToken())
            .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = stored.getUser();
        refreshTokenRepository.delete(stored);
        return issueTokenPair(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
            .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        otpService.validate(user, OtpType.EMAIL_VERIFICATION, req.code());
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerification(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        long attempts = otpService.countRecentAttempts(
            user, OtpType.EMAIL_VERIFICATION,
            LocalDateTime.now().minusHours(resendWindowHours)
        );

        if (attempts >= maxResendAttempts) {
            throw new AppException(ErrorCode.RESEND_LIMIT_EXCEEDED);
        }

        String code = otpService.generateAndSave(user, OtpType.EMAIL_VERIFICATION, 15);
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), code);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmail(req.email().toLowerCase()).ifPresent(user -> {
            String code = otpService.generateAndSave(user, OtpType.PASSWORD_RESET, 15);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), code);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        otpService.validate(user, OtpType.PASSWORD_RESET, req.code());
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        refreshTokenRepository.deleteAllByUserId(user.getId());
        userRepository.save(user);
    }

    @Transactional
    public void enableTwoFactor(User user) {
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void sendTwoFactorCode(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String code = otpService.generateAndSave(user, OtpType.TWO_FACTOR, 5);
            emailService.sendTwoFactorCode(user.getEmail(), user.getName(), code);
        });
    }

    public TokenResponse issueTokenPair(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String rawRefresh = jwtUtil.generateRefreshToken(user.getId());

        RefreshToken refreshEntity = RefreshToken.builder()
            .token(rawRefresh)
            .user(user)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();
        refreshTokenRepository.save(refreshEntity);

        return TokenResponse.of(accessToken, rawRefresh, jwtUtil.getAccessExpiryMs(), UserAuthResponse.from(user));
    }
}
