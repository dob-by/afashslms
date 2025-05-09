package com.afashslms.demo.service;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.dto.SignupForm;
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

    public void registerUser(SignupForm form) {
        // 1. 아이디 또는 이메일 중복 확인
        if (userRepository.findByUserId(form.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2. 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(form.getPassword());

        // 3. User 엔티티 생성
        User user = new User();
        user.setUserId(form.getUserId());
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(encodedPassword);
        user.setRole(Role.valueOf(form.getRole())); //가입자 유형
        user.setProvider("local");
        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        user.setMilitaryId(form.getMilitaryId());

        // 4. 저장
        userRepository.save(user);
    }
}