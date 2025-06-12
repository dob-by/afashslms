package com.afashslms.demo.service.impl;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
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

    @Override
    public long countPendingAdmins() {
        return userRepository.countByRole(Role.PENDING_ADMIN);
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public void updateAdminProfile(String userId, String username, String militaryId, String affiliation, String unit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setUsername(username);
        user.setMilitaryId(militaryId);
        user.setAffiliation(affiliation);

        if ("학생대".equals(affiliation)) {
            user.setUnit(unit); // 1중대, 2중대, 3중대
        } else {
            user.setUnit(null); // 교육대는 중대 없음
        }

        userRepository.save(user);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void registerPendingAdmin(String email, String provider, String username, String militaryId, String affiliation, String unit) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        System.out.println("registerPendingAdmin 접근");

        if (userOpt.isEmpty()) {
            log.error("❌ 존재하지 않는 이메일로 등록 시도: {}", email);
            return;
        }

        User user = userOpt.get();
        user.setUsername(username);
        user.setMilitaryId(militaryId);
        user.setAffiliation(affiliation);
        user.setUnit("학생대".equals(affiliation) ? unit : null);
        user.setRole(Role.PENDING_ADMIN);
        user.setProfileCompleted(true);

        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        userRepository.saveAndFlush(user); // 변경 사항 즉시 반영

        // ✅ 최신 상태로 다시 조회해서 보장
        User refreshedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ 저장 후 사용자 조회 실패"));

        // ✅ 새 권한 기반 Principal 생성
        CustomOAuth2User updatedPrincipal = new CustomOAuth2User(
                refreshedUser,
                Map.of("email", email),
                email,
                provider,
                false
        );

        OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
                updatedPrincipal,
                updatedPrincipal.getAuthorities(),
                provider
        );

        // ✅ 시큐리티 컨텍스트에 갱신
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        log.info("🔁 권한 갱신됨: {}", updatedPrincipal.getAuthorities());
        log.info("✅ 관리자 등록 완료: {} / {}", email, refreshedUser.getRole());
    }

    @Override
    public boolean approvePendingAdmin(String userId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 🔥 프로필 미입력 시 승인 불가
            if (!user.isProfileComplete()) {
                return false;
            }

            if (user.getRole() == Role.PENDING_ADMIN) {
                user.setRole(Role.MID_ADMIN);
                userRepository.save(user);
                return true;
            }
        }

        return false;
    }
}