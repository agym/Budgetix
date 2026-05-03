package com.budgetix.config;

import com.budgetix.auth.dto.TokenResponse;
import com.budgetix.auth.service.AuthService;
import com.budgetix.user.entity.User;
import com.budgetix.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new IllegalStateException("OAuth2 user not found: " + email));

        TokenResponse tokens = authService.issueTokenPair(user);

        String redirect = frontendUrl + "/auth/oauth2/callback"
            + "?accessToken=" + URLEncoder.encode(tokens.accessToken(), StandardCharsets.UTF_8)
            + "&refreshToken=" + URLEncoder.encode(tokens.refreshToken(), StandardCharsets.UTF_8);

        response.sendRedirect(redirect);
    }
}
