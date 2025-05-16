package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.LaptopStatus;
import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.UserService;
import com.afashslms.demo.repository.LaptopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final LaptopRepository laptopRepository;

    @GetMapping("/admin/users")
    public String showUserList(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<User> users = userService.getAllUsers();

        if (userDetails != null) {
            model.addAttribute("users", users);
            model.addAttribute("username", userDetails.getUser().getUsername());
            model.addAttribute("userRole", userDetails.getRole().name());
        }

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

    @GetMapping("/admin/users/{userId}")
    public String getUserDetail(@PathVariable String userId, Model model,
                                @AuthenticationPrincipal CustomUserDetails loginUser) throws AccessDeniedException {
        List<User> users = userService.getAllUsers();

        if (loginUser != null) {
            model.addAttribute("users", users);
            model.addAttribute("username", loginUser.getUser().getUsername());
            model.addAttribute("userRole", loginUser.getRole().name());
        }

        if (loginUser == null || loginUser.getRole() != Role.TOP_ADMIN) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        User user = userService.findByUserId(userId);
        List<Laptop> laptops = laptopRepository.findByUser_UserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("laptops", laptops);
        return "admin/user-detail";
    }

    @PostMapping("/admin/users/{userId}/laptops/{deviceId}/status")
    public String updateLaptopStatus(@PathVariable String userId,
                                     @PathVariable String deviceId,
                                     @RequestParam LaptopStatus newStatus) {
        Laptop laptop = laptopRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 노트북 없음"));
        laptop.setStatus(newStatus);
        laptopRepository.save(laptop);
        return "redirect:/admin/users/" + userId; // 다시 사용자 상세로 이동
    }
}