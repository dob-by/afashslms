package com.afashslms.demo.controller;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KakaoLoginController {

    @GetMapping("/login/success")
    public String getKakaoUserInfo(OAuth2User oAuth2User) {
        // 카카오 사용자 정보 가져오기
        System.out.println("로그인 성공한 유저 정보: " + oAuth2User.getAttributes());
        return "로그인 성공";
    }
}