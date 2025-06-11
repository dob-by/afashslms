package com.afashslms.demo.service.impl;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void changeRole(String userId, String newRole) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Role roleEnum = Role.valueOf(newRole); // "MID_ADMIN" → Role.MID_ADMIN
        user.setRole(roleEnum);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> searchUsers(String query) {
        return userRepository.findByUserIdContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query);
    }

    @Override
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    }

    @Override
    public List<User> searchByUsernameOrUserId(String keyword) {
        return userRepository.findByUsernameContainingIgnoreCaseOrUserIdContainingIgnoreCase(keyword, keyword);
    }

    @Override
    public boolean updatePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        System.out.println("✅ 비번 변경 로직 진입: " + email);
        System.out.println("✅ DB 비번: " + user.getPassword());
        System.out.println("✅ 매칭 여부: " + passwordEncoder.matches(currentPassword, user.getPassword()));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // 틀림
        }

        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(newPassword));

        user.setRole(Role.PENDING_ADMIN); //승인 대기 중 상태로 저장

        userRepository.save(user);
        return true;
    }
}