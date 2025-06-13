package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.LaptopService;
import com.afashslms.demo.service.RepairService;
import com.afashslms.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final LaptopService laptopService;
    private final RepairService repairService;
    private final UserService userService;

    @GetMapping("")
    public String mypage(@AuthenticationPrincipal Object principal, Model model, HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String username;
        String email;
        String role;
        User user;

        if (principal instanceof CustomUserDetails userDetails) {
            user = userDetails.getUser();
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            user = oauthUser.getUser();
        } else {
            return "redirect:/login";
        }

        username = user.getUsername();
        email = user.getEmail();
        role = user.getRole().name();

        // ✅ 최신 사용자 정보 조회 (DB에서)
        User latestUser = userService.findByEmail(email).orElse(user); // Optional 대응도 안전하게

        // 관리자면 관리자 마이페이지로 이동
        if ("MID_ADMIN".equals(role) || "TOP_ADMIN".equals(role)) {
            return "redirect:/admin/mypage";
        }

        // 학생인데 비밀번호를 한 번도 안 바꿨다면 비번 변경 페이지로 강제 이동
        if ("STUDENT".equals(role) && !user.isPasswordChanged()) {
            System.out.println("[DEBUG] 비밀번호 미변경 학생입니다.");
            model.addAttribute("passwordChanged", false);

            model.addAttribute("firstLogin", true);
        } else {
            model.addAttribute("passwordChanged", true);
        }

        // ✅ 공통 모델 등록
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
                                 @AuthenticationPrincipal Object principal,
                                 RedirectAttributes redirectAttributes, HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {

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
            email = user.getEmail();
            username = user.getUsername();
            role = user.getRole().toString();
        } else {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("passwordError", "새 비밀번호가 서로 일치하지 않습니다.");
            model.addAttribute("passwordChanged", false);
            return "mypage/mypage"; // ❗ 직접 뷰 이름을 반환
        }

        if (!userService.updatePassword(email, currentPassword, newPassword)) {
            model.addAttribute("passwordError", "현재 비밀번호가 올바르지 않습니다.");
            model.addAttribute("passwordChanged", false);
            return "mypage/mypage"; // ❗ 여기서도 마찬가지
        }


        // ✅ 성공했을 때는 플래시로 메시지 넘기고 홈으로
        redirectAttributes.addFlashAttribute("passwordSuccess", "비밀번호가 성공적으로 변경되었습니다!");


        // ✅ 인증 정보 갱신 (핵심!!)
        User updatedUser = userService.findByEmail(email).orElseThrow();
        CustomUserDetails updatedPrincipal = new CustomUserDetails(updatedUser);
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                updatedPrincipal,
                null,
                updatedPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);


        return "redirect:/";
    }

    @GetMapping("/password")
    public String showPasswordForm(@RequestParam(required = false) String firstChange,
                                   Model model) {
        if ("true".equals(firstChange)) {
            model.addAttribute("firstChangeNotice", "🔐 보안을 위해 비밀번호를 먼저 변경해주세요.");
        }
        return "mypage/mypage"; // 기존 마이페이지 HTML 그대로 사용
    }
}