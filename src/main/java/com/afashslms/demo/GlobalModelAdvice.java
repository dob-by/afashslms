package com.afashslms.demo;
import com.afashslms.demo.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getEmail() : null;
    }

    @ModelAttribute("userRole")
    public String userRole(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getRole().getDisplayName() : null;
    }

    @ModelAttribute("username")
    public String username(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getUser().getUsername() : "알 수 없음";
    }
}