package com.afashslms.demo.controller;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

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
                return userDetails.getUsername();
            }
        }
        return "알 수 없음";
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();
            System.out.println("🔍 Principal 타입: " + principal.getClass().getName());

            // OAuth2 로그인 사용자
            if (principal instanceof CustomUserDetails customUser) {
                User user = customUser.getUser();
                System.out.println(">>> user class = " + user.getClass());
                System.out.println(">>> user.getRole() = " + user.getRole());
                System.out.println(">>> user.getRole() type = " + user.getRole().getClass());

                model.addAttribute("userRole", user.getRole().getDisplayName());
            }

            // 로컬 로그인 사용자
            if (principal instanceof CustomUserDetails userDetails) {
                model.addAttribute("userRole", userDetails.getRole().name());
                model.addAttribute("username", userDetails.getUser().getUsername());
                return;
            }

            // 문자열 (로그인된 이메일로 들어오는 경우)
            if (principal instanceof String str && !str.equals("anonymousUser")) {
                model.addAttribute("userRole", "USER");
                model.addAttribute("username", str);
                return;
            }
        }

        // 로그인 안 한 사용자
        model.addAttribute("userRole", "ANONYMOUS");
        model.addAttribute("username", "손님");
    }
}