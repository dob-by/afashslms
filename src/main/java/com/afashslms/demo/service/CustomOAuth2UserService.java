package com.afashslms.demo.service;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = getEmail(userRequest, oAuth2User);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 기존 사용자 조회
        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        // 신규 사용자일 경우 저장
        User user = existingUserOpt.orElseGet(() -> {
            User newUser = new User();
            newUser.setUserId("oauth_" + UUID.randomUUID().toString().substring(0, 8));
            newUser.setEmail(email);
            newUser.setUsername((String) attributes.get("name"));
            newUser.setProvider(registrationId);
            newUser.setOauthId(oAuth2User.getName());
            newUser.setRole(Role.TEMP);  // 최초 로그인 시 기본 권한 TEMP
            return userRepository.save(newUser);
        });

        // 로그인 차단 조건 (예: 승인 대기중)
        if (user.getRole() == Role.PENDING_ADMIN) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("access_denied", "관리자 승인 대기 중입니다.", null)
            );
        }

        // CustomOAuth2User 반환
        return new CustomOAuth2User(
                user,
                attributes,
                email,
                registrationId,
                existingUserOpt.isEmpty() // 최초 로그인 여부 전달
        );
    }

    private static String getEmail(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = null;

        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");

        } else if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다.");
        }

        return email;
    }
}