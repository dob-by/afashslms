package com.afashslms.demo;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoginIntegrationTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 디버깅용_비밀번호확인() {
        UserDetails user = userDetailsService.loadUserByUsername("admin");
        System.out.println("DB 비번 (해시): " + user.getPassword());
        System.out.println("입력 비번: 1234");
        System.out.println("일치 여부: " + passwordEncoder.matches("1234", user.getPassword()));
    }

    @Test
    void 로그인_정상_성공() {
        User user = createTestUser("admin", "1234");
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertThat(passwordEncoder.matches("1234", userDetails.getPassword()));
    }

    @Test
    void 로그인_실패_아이디_없음() {
        // given
        String invalidUserId = "nonexistent";

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(invalidUserId);
        });
    }

    @Test
    void 로그인_실패_비밀번호_불일치() {
        // given
        userRepository.save(createTestUser("admin", "1234"));

        // when
        UserDetails user = userDetailsService.loadUserByUsername("admin");
        boolean matches = passwordEncoder.matches("wrongpass", user.getPassword());

        // then
        assertFalse(matches);
    }


    private User createTestUser(String userId, String rawPassword) {
        User user = new User();
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setUsername("관리자");
        user.setEmail("admin@test.com");
        user.setProvider("local");
        user.setRole(Role.TOP_ADMIN);
        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return user;
    }
}