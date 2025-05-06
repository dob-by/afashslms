package com.afashslms.demo.controller;

import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHome(Model model, Authentication authentication) {
        String roleName = "GUEST"; // 기본값

        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            roleName = oAuth2User.getUser().getRole().name(); // STUDENT 등
        } else if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            roleName = userDetails.getUser().getRole().name();
        }

        model.addAttribute("userRole", roleName);
        return "home";
    }
}
