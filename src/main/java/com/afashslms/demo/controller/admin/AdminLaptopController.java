package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.LaptopStatus;
import com.afashslms.demo.domain.Role;
import com.afashslms.demo.dto.LaptopViewDto;
import com.afashslms.demo.repository.LaptopRepository;
import com.afashslms.demo.service.LaptopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.afashslms.demo.security.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminLaptopController {

    private final LaptopService laptopService;
    private final LaptopRepository laptopRepository;

    @GetMapping("/admin/laptops")
    public String showLaptopList(Model model) {
        List<LaptopViewDto> laptops = laptopService.getAllLaptopsForAdmin();
        model.addAttribute("laptops", laptops);
        return "admin/laptop-list";
    }

    @GetMapping("/admin/laptops/{deviceId}")
    public String laptopDetail(@PathVariable String deviceId,
                               @AuthenticationPrincipal CustomUserDetails loginUser,
                               Model model) throws AccessDeniedException {
        if (loginUser == null || loginUser.getRole() != Role.TOP_ADMIN) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        Laptop laptop = laptopRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("노트북을 찾을 수 없습니다."));

        model.addAttribute("laptop", laptop);
        return "admin/laptop-detail";
    }

    @PostMapping("/admin/laptops/updateStatus")
    public String updateStatus(@RequestParam String deviceId,
                               @RequestParam LaptopStatus status,
                               @RequestHeader("Referer") String referer) {
        Laptop laptop = laptopRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트북 없음"));
        laptop.setStatus(status);
        laptopRepository.save(laptop);

        // 기존 페이지로 리다이렉트 (ex: 사용자 상세 페이지)
        return "redirect:" + referer;
    }
}