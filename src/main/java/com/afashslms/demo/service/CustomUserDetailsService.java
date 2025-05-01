package com.afashslms.demo.service;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 디버깅 로그
        System.out.println(">>> user.getUserId(): " + user.getUserId());
        System.out.println(">>> user.getPassword(): " + user.getPassword());
        System.out.println(">>> matches(1234): " + passwordEncoder.matches("1234", user.getPassword()));

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(), // username
                user.getPassword(), // password
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}