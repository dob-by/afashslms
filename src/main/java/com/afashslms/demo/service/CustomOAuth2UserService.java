package com.afashslms.demo.service;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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

        // 고유 식별자 키 가져오기 (ex. 구글: sub, 카카오: id)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 기존 회원 있는지 확인
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 없으면 새로 가입
                    User newUser = new User();
                    newUser.setUserId(UUID.randomUUID().toString());
                    newUser.setEmail(email);
                    newUser.setUsername(registrationId + "_user_" + email); // 구글/카카오 구분용
                    newUser.setProvider(registrationId);
                    newUser.setRole(Role.USER);
                    return userRepository.save(newUser);
                });

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                userNameAttributeName
        //기존 회원 있는지 확인
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    //없으면 새로 가입
                    User newUser = new User();
                    newUser.setUserId(UUID.randomUUID().toString());
                    newUser.setEmail(email);
                    newUser.setUsername("kakao_user_" + email);
                    newUser.setProvider("kakao");
                    newUser.setRole(Role.USER);  // 기본 권한
                    return userRepository.save(newUser);
                });
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                "id" //카카오에서 고유 식별자 (attributes에 "id"있음)

        );
    }

    private static String getEmail(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = null;

        if (registrationId.equals("kakao")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");

        } else if (registrationId.equals("google")) {
            email = (String) attributes.get("email"); // 구글은 그냥 email 키에 들어있음
        }
        }
        if (email == null) {
            throw new OAuth2AuthenticationException("카카오 계정에 이메일이 없습니다.");
        }
        return email;
    }

