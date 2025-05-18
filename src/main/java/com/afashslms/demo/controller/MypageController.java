package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.LaptopService;
import com.afashslms.demo.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final LaptopService laptopService;
    private final RepairService repairService;

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

        if ("STUDENT".equals(role)) {
            Laptop laptop = laptopService.findCurrentLaptopByEmail(email);
            List<RepairRequest> repairs = repairService.findAllByStudentEmail(email);

            System.out.println("🔍 로그인 유저 이메일: " + email);
            System.out.println("🔍 불러온 수리 요청 개수: " + repairs.size());
            repairs.forEach(r -> System.out.println("➡️ " + r.getCreatedAt() + " / " + r.getDetailType()));

            model.addAttribute("laptop", laptop);
            model.addAttribute("repairs", repairs);
        }

        return "mypage/mypage";
    }


}