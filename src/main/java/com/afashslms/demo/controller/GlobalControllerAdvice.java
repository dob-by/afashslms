package com.afashslms.demo.controller;

import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("userRole")
    public String getUserRole(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return userDetails.getRole().name();
            }
        }
        return null;
    }

    @ModelAttribute("username")
    public String getUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return userDetails.getUsername(); // 또는 getName(), getEmail() 등 원하는 항목
            }
        }
        return "알 수 없음";
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof com.afashslms.demo.security.CustomOAuth2User user) {
                model.addAttribute("userRole", user.getRole().name());
                model.addAttribute("username", user.getName());
                return;
            }

            // 로컬 로그인 (CustomUserDetails가 Principal로 들어왔을 경우)
            if (principal instanceof org.springframework.security.core.userdetails.User user) {
                // DB에서 직접 조회하는 서비스로 사용자 role 꺼내기
                // 예시: userService.findByEmail(user.getUsername()).getRole()
                // 간단하게 일단 ROLE_USER 고정 처리
                model.addAttribute("userRole", "USER");
                model.addAttribute("username", user.getUsername());
                return;
            }

            // 혹시 그냥 이메일(String)로 들어올 경우
            if (principal instanceof String email) {
                model.addAttribute("userRole", "USER");
                model.addAttribute("username", email);
                return;
            }
        }

        // 로그인 안 된 경우
        model.addAttribute("userRole", "ANONYMOUS");
        model.addAttribute("username", "손님");
    }

}