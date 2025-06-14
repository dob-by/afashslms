package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.domain.Role;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminApprovalController {

    private static final Logger log = LoggerFactory.getLogger(AdminApprovalController.class);

    private final UserService userService;

    @GetMapping("/pending-admins")
    public String pendingAdmins(Model model) {
        List<User> pendingList = userService.findByRole(Role.PENDING_ADMIN);
        model.addAttribute("pendingAdmins", pendingList);
        return "admin/pending-admin-list";
    }

    @PostMapping("/approve/{userId}")
    public String approvePendingAdmin(@PathVariable String userId, RedirectAttributes redirectAttributes) {
        System.out.println("approvePendingAdmin 접근");

        User user = userService.findByUserId(userId);

        boolean result = userService.approvePendingAdmin(userId);

        if (result) {
            redirectAttributes.addFlashAttribute("successMessage", "승인이 완료되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "승인 중 오류가 발생했습니다.");
        }

        return "redirect:/admin/pending-admins";
    }
}