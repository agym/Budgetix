package com.budgetix.auth.service;

import com.budgetix.user.entity.User;
import com.budgetix.user.entity.UserProfile;
import com.budgetix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        String avatar     = oAuth2User.getAttribute("picture");

        userRepository.findByEmail(email.toLowerCase()).ifPresentOrElse(
            existing -> {
                // Update avatar if changed
                if (avatar != null && !avatar.equals(existing.getAvatar())) {
                    existing.setAvatar(avatar);
                    userRepository.save(existing);
                }
            },
            () -> {
                UserProfile profile = UserProfile.builder().build();
                User user = User.builder()
                    .email(email.toLowerCase())
                    .name(name != null ? name : email)
                    .avatar(avatar)
                    .provider("GOOGLE")
                    .providerId(providerId)
                    .emailVerified(true)
                    .profile(profile)
                    .build();
                profile.setUser(user);
                userRepository.save(user);
            }
        );

        return oAuth2User;
    }
}
