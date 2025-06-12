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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.afashslms.demo.dto.UserDto;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final LaptopRepository laptopRepository;

    @GetMapping("/admin/users")
    public String showUserList(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUser().getUsername());
            model.addAttribute("userRole", userDetails.getRole().name());
        }

        return "admin/user-list";
    }


    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<UserDto>> getUsersApi() {
        List<UserDto> dtos = userService.getAllUsers().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/admin/users/{userId}/role")
    @PreAuthorize("hasRole('TOP_ADMIN')")
    public String changeUserRole(@PathVariable String userId,
                                 @RequestParam String newRole,
                                 @AuthenticationPrincipal CustomUserDetails loginUser) throws AccessDeniedException {
        if (loginUser == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        // Optional에서 꺼낸 값을 user에 저장
        User user = userService.findByEmail(loginUser.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // user.getRole()로 검사
        if (user.getRole() != Role.TOP_ADMIN) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        userService.changeRole(userId, newRole);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasAnyRole('MID_ADMIN', 'TOP_ADMIN')")
    public String getUserDetail(@PathVariable String userId, Model model,
                                @AuthenticationPrincipal CustomUserDetails loginUser) {
        List<User> users = userService.getAllUsers();

        model.addAttribute("users", users);
        model.addAttribute("username", loginUser.getUser().getUsername());
        model.addAttribute("userRole", loginUser.getRole().name());

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