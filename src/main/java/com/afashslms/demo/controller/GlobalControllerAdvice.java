package com.afashslms.demo.controller;

import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();

            if (principal instanceof com.afashslms.demo.security.CustomOAuth2User user) {
                model.addAttribute("userRole", user.getRole().name());
                model.addAttribute("username", user.getName());
                return;
            }

            if (principal instanceof org.springframework.security.core.userdetails.User user) {
                model.addAttribute("userRole", "USER");
                model.addAttribute("username", user.getUsername());
                return;
            }

            if (principal instanceof String str) {
                if (!str.equals("anonymousUser")) {
                    model.addAttribute("userRole", "USER");
                    model.addAttribute("username", str);
                } else {
                    model.addAttribute("userRole", "ANONYMOUS");
                    model.addAttribute("username", "손님");
                }
                return;
            }
        }

        // 로그인 안 된 경우
        model.addAttribute("userRole", "ANONYMOUS");
        model.addAttribute("username", "손님");
    }

}