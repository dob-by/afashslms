package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.RepairStatus;
import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.RepairService;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminMypageController {

    private final RepairService repairService;
    private final UserService userService;

    @GetMapping("/mypage")
    public String adminMypage(Model model, @AuthenticationPrincipal Object principal) {
        User user;

        if (principal instanceof CustomOAuth2User oauthUser) {
            user = oauthUser.getUser();
        } else if (principal instanceof CustomUserDetails userDetails) {
            user = userDetails.getUser();
        } else {
            throw new IllegalStateException("알 수 없는 로그인 유형입니다.");
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        // 총괄 관리자일 때만 승인 대기 관리자 수 보여주기
        if (user.getRole() == Role.TOP_ADMIN) {
            model.addAttribute("pendingAdmins", userService.countPendingAdmins());
        }

        // 수리 통계
        model.addAttribute("totalRepairs", repairService.countAll());
        model.addAttribute("pendingRepairs", repairService.countByStatusNot(RepairStatus.COMPLETED));
        model.addAttribute("completedRepairs", repairService.countByStatus(RepairStatus.COMPLETED));
        model.addAttribute("weeklyRepairs", repairService.countThisWeek());

        return "admin/mypage";
    }
}
