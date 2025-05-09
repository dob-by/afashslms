package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/admin/users")
    public String showUserList(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user-list";
    }

    @PostMapping("/admin/users/{userId}/role")
    public String changeUserRole(@PathVariable String userId,
                                 @RequestParam String newRole,
                                 @AuthenticationPrincipal CustomUserDetails loginUser) throws AccessDeniedException {
        if (loginUser == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        // 이메일로 사용자 조회
        User currentUser = userService.findByEmail(loginUser.getEmail());

        if (currentUser.getRole() != Role.TOP_ADMIN) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        userService.changeRole(userId, newRole);
        return "redirect:/admin/users";
    }
}