package com.afashslms.demo.controller;

import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MypageController {

    // 나중에 구현 예정
    // private final LaptopService laptopService;
    // private final RepairService repairService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal Object principal, Model model) {
        String username;
        String email;
        String role;

        if (principal instanceof CustomUserDetails userDetails) {
            username = userDetails.getUser().getUsername();
            email = userDetails.getUser().getEmail();
            role = String.valueOf(userDetails.getUser().getRole());
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            username = oauthUser.getName();
            email = oauthUser.getEmail();
            role = String.valueOf(oauthUser.getRole());
        } else {
            return "redirect:/login"; // 로그인 안 한 사용자
        }

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("userRole", role);

        /*
        // 나중에 STUDENT일 경우 노트북 정보와 수리내역 표시
        if ("STUDENT".equals(role)) {
            Laptop laptop = laptopService.findByEmail(email);
            List<Repair> repairs = repairService.findAllByEmail(email);
            model.addAttribute("laptop", laptop);
            model.addAttribute("repairs", repairs);
        }
        */

        return "mypage/mypage";
    }
}