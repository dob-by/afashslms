package com.afashslms.demo.config;

import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addGlobalAttributes(Model model, @AuthenticationPrincipal Object principal) {
        if (principal instanceof CustomUserDetails customUser) {
            model.addAttribute("username", customUser.getUser().getUsername());
            model.addAttribute("userRole", customUser.getUser().getRole().name());
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            model.addAttribute("username", oauthUser.getUser().getUsername());
            model.addAttribute("userRole", oauthUser.getUser().getRole().name());
        }
    }
}