package com.afashslms.demo.service;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        System.out.println(">>> OAuth2UserRequest: " + userRequest);
        System.out.println(">>> Access Token: " + userRequest.getAccessToken().getTokenValue());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = getEmail(userRequest, oAuth2User);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 기존 사용자 조회
        Optional<User> optionalUser = userRepository.findByEmail(email);

        // 없으면 로그인 차단 + insert 안함
        if (optionalUser.isEmpty()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unauthorized", "최초 로그인입니다. 관리자 승인이 필요합니다.", null)
            );
        }

        User user = optionalUser.get();

        // 승인 대기 상태인 경우 로그인 차단
        if (user.getRole() == Role.PENDING_ADMIN) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("access_denied", "관리자 승인 대기 중입니다.", null)
            );
        }

        // 정상 로그인
        return new CustomOAuth2User(user, attributes);
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