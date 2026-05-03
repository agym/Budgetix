package com.budgetix.user.service;

import com.budgetix.common.exception.AppException;
import com.budgetix.common.exception.ErrorCode;
import com.budgetix.user.entity.User;
import com.budgetix.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return toUserDetails(user);
    }

    public UserDetails loadUserByUserId(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return toUserDetails(user);
    }

    private UserDetails toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
            user.getId().toString(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
