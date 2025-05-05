package com.afashslms.demo.controller;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/repairs")
public class RepairController {

    private final RepairService repairService;
    private final UserRepository userRepository;

    @GetMapping("/new")
    public String showRepairForm() {
        return "repair/new";  // templates/repair/new.html
    }

    // ✅ 수리 요청서 제출 처리
    @PostMapping
    public String submitRepair(@ModelAttribute RepairRequest repairRequest, Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String email = oauthToken.getPrincipal().getAttribute("email");
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("OAuth 사용자 정보를 찾을 수 없습니다."));
        } else {
            String userId = principal.getName();
            user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Local 사용자 정보를 찾을 수 없습니다."));
        }

        repairRequest.setUser(user);
        repairService.saveRepairRequest(repairRequest);
        return "redirect:/repairs";
    }

    @GetMapping  // URL은 /repairs
    public String showRepairList(Model model, Principal principal) {
        String userId = principal.getName();
        List<RepairRequest> repairs = repairService.getRepairsByUser(userId);
        RepairRequest recentRepair = repairs.isEmpty() ? null : repairs.get(0);
        long openRepairsCount = repairs.stream().filter(r -> !r.getStatus().equals("완료")).count();

        model.addAttribute("repairs", repairs);
        model.addAttribute("recentRepair", recentRepair);
        model.addAttribute("openRepairsCount", openRepairsCount);
        return "repair/list";
    }
}