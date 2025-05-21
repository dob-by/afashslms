package com.afashslms.demo.service.impl;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
}