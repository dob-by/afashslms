package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final UserService userService;

    // 프로필 작성 폼
    @GetMapping
    public String showProfileForm(
            HttpSession session,
            Model model,
            @RequestParam(value = "error", required = false) String error
    ) {
        String email = (String) session.getAttribute("oauthEmail");
        String provider = (String) session.getAttribute("oauthProvider");

        session.setAttribute("email", email);
        session.setAttribute("provider", provider);

        if (email == null || provider == null) {
            return "redirect:/login"; // 세션에 없으면 로그인으로
        }

        model.addAttribute("email", email);
        model.addAttribute("provider", provider);

        // 에러 메시지 있으면 템플릿으로 전달
        if ("need_profile".equals(error)) {
            model.addAttribute("errorMessage", "추가 관리자 정보를 입력하셔야 이용할 수 있습니다.");
        }

        return "admin/profile-form";
    }

    // 프로필 제출
    @PostMapping
    public String submitProfile(
            HttpSession session,
            @RequestParam String username,
            @RequestParam String militaryId,
            @RequestParam String affiliation,
            @RequestParam(required = false) String unit
    ) {

        System.out.println("✅✅✅ submitProfile POST 요청 들어옴");
        String email = (String) session.getAttribute("email");
        String provider = (String) session.getAttribute("provider");

        if (email == null || provider == null) {
            return "redirect:/login";
        }

        System.out.println("✅ 프로필 제출 - 이메일: " + email + " / provider: " + provider);
        userService.registerPendingAdmin(email, provider, username, militaryId, affiliation, unit);

        // 세션 초기화 후 로그인 페이지로
        session.invalidate();

        String successMessage = URLEncoder.encode("정보가 등록되었습니다. 승인 후 로그인 가능합니다.", StandardCharsets.UTF_8);
        return "redirect:/login?successMessage=" + successMessage;
    }
}