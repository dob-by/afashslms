package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.LaptopService;
import com.afashslms.demo.service.RepairService;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final LaptopService laptopService;
    private final RepairService repairService;
    private final UserService userService;

    @GetMapping("")
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
            return "redirect:/login";
        }

        if ("MID_ADMIN".equals(role) || "TOP_ADMIN".equals(role)) {
            return "redirect:/admin/mypage";
        }

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("userRole", role);

        if ("STUDENT".equals(role)) {
            Laptop laptop = laptopService.findCurrentLaptopByEmail(email);
            List<RepairRequest> repairs = repairService.findAllByStudentEmail(email);

            model.addAttribute("laptop", laptop);
            model.addAttribute("repairs", repairs);
        }

        return "mypage/mypage";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model,
                                 @AuthenticationPrincipal Object principal) {

        String userId = null;
        String email = null;
        String username = null;
        String role = null;

        if (principal instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            userId = user.getUserId();
            email = user.getEmail();
            username = user.getUsername();
            role = user.getRole().toString();
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            User user = oauthUser.getUser();
            userId = user.getUserId();
            email = user.getEmail(); //
            username = user.getUsername();
            role = user.getRole().toString();
        } else {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("passwordError", "새 비밀번호가 서로 일치하지 않습니다.");
        } else if (!userService.updatePassword(email, currentPassword, newPassword)) {
            model.addAttribute("passwordError", "현재 비밀번호가 올바르지 않습니다.");
        } else {
            model.addAttribute("passwordSuccess", "비밀번호가 성공적으로 변경되었습니다!");
        }

        return mypage(principal, model); // 마이페이지 재렌더링
    }
}