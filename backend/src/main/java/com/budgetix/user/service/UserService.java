package com.budgetix.user.service;

import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.user.dto.ChangePasswordRequest;
import com.budgetix.user.dto.UpdateProfileRequest;
import com.budgetix.user.dto.UpdateSettingsRequest;
import com.budgetix.user.dto.UserResponse;
import com.budgetix.user.entity.User;
import com.budgetix.user.entity.UserProfile;
import com.budgetix.user.repository.UserProfileRepository;
import com.budgetix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getMe(UUID userId) {
        User user = userRepository.findByIdWithProfile(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (req.name() != null) user.setName(req.name());
        if (req.avatar() != null) user.setAvatar(req.avatar());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateSettings(UUID userId, UpdateSettingsRequest req) {
        UserProfile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (req.currency() != null) profile.setCurrency(req.currency());
        if (req.monthlyIncome() != null) profile.setMonthlyIncome(req.monthlyIncome());
        if (req.financialGoalType() != null) profile.setFinancialGoalType(req.financialGoalType());
        if (req.notifyBudgetAlerts() != null) profile.setNotifyBudgetAlerts(req.notifyBudgetAlerts());
        if (req.notifyGoalReminders() != null) profile.setNotifyGoalReminders(req.notifyGoalReminders());
        if (req.notifyWeeklySummary() != null) profile.setNotifyWeeklySummary(req.notifyWeeklySummary());
        if (req.timezone() != null) profile.setTimezone(req.timezone());

        profileRepository.save(profile);
        return getMe(userId);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    public User getEntity(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public List<UUID> findAllVerifiedUserIds() {
        return userRepository.findAllVerifiedUserIds();
    }
}
