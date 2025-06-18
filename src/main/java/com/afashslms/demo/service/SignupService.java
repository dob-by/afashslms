package com.afashslms.demo.service;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.dto.SignupFormDto;
import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(SignupFormDto form) {
        // 아이디 또는 이메일 중복 확인
        if (userRepository.findByUserId(form.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(form.getPassword());

        // User 엔티티 생성
        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(encodedPassword);

        // 승인 대기 상태로 설정
        user.setRole(Role.PENDING_ADMIN);

        user.setProvider("local");
        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        user.setMilitaryId(form.getUserId());
        user.setUserId(form.getUserId()); // 군번 = 아이디 = userId
        user.setProfileCompleted(true); // 로컬은 가입 시 프로필 완성된 것으로 간주

        userRepository.save(user);
    }
}