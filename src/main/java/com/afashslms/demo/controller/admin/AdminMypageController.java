package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.RepairStatus;
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
    public String adminMypage(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        model.addAttribute("username", principal.getUser().getUsername());
        model.addAttribute("email", principal.getUser().getEmail());
        model.addAttribute("userRole", principal.getUser().getRole().getDisplayName());
        model.addAttribute("pendingAdmins", userService.countPendingAdmins());

        // 통계 데이터
        model.addAttribute("totalRepairs", repairService.countAll());
        model.addAttribute("pendingRepairs", repairService.countByStatusNot(RepairStatus.COMPLETED));
        model.addAttribute("completedRepairs", repairService.countByStatus(RepairStatus.COMPLETED));
        model.addAttribute("weeklyRepairs", repairService.countThisWeek());
        model.addAttribute("pendingAdmins", userService.countPendingAdmins());

        return "admin/mypage";
    }
}
