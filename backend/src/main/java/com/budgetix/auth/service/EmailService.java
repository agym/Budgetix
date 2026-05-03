package com.budgetix.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail-from}")
    private String mailFrom;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String name, String code) {
        String subject = "Verify your Budgetix account";
        String body = String.format("""
            Hi %s,

            Welcome to Budgetix! Please verify your email address using the code below:

            Code: %s

            This code expires in 15 minutes.

            If you did not create an account, please ignore this email.

            — The Budgetix Team
            """, name, code);
        send(to, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String code) {
        String subject = "Reset your Budgetix password";
        String body = String.format("""
            Hi %s,

            We received a request to reset your password.

            Reset code: %s

            This code expires in 15 minutes.

            If you did not request a password reset, please ignore this email.

            — The Budgetix Team
            """, name, code);
        send(to, subject, body);
    }

    @Async
    public void sendTwoFactorCode(String to, String name, String code) {
        String subject = "Your Budgetix login code";
        String body = String.format("""
            Hi %s,

            Your two-factor authentication code is:

            %s

            This code expires in 5 minutes.

            — The Budgetix Team
            """, name, code);
        send(to, subject, body);
    }

    @Async
    public void sendWeeklySummary(String to, String name, double income, double expenses, double savings) {
        String subject = "Your Budgetix weekly summary";
        String body = String.format("""
            Hi %s,

            Here's your financial summary for this week:

            💰 Income:   %.2f
            💸 Expenses: %.2f
            🏦 Saved:    %.2f

            Keep up the great work!

            — The Budgetix Team
            """, name, income, expenses, savings);
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.debug("Email sent to {} - {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
