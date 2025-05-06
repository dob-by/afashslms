package com.afashslms.demo.controller;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/repairs")
public class RepairController {

    private final RepairService repairService;
    private final UserRepository userRepository;

    // 공통으로 사용할 유틸 메서드
    private String extractAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
            if (attributes.containsKey("kakao_account")) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                return (String) kakaoAccount.get("email");
            } else {
                return (String) attributes.get("email"); // Google
            }
        } else if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getEmail(); // Local login
        } else {
            throw new RuntimeException("인증된 사용자 정보를 가져올 수 없습니다.");
        }
    }

    @GetMapping("/new")
    public String showRepairForm() {
        return "repair/new";  // templates/repair/new.html
    }

    // 수리 요청서 제출 처리
    @PostMapping
    public String submitRepair(@ModelAttribute RepairRequest repairRequest) {
        String email = extractAuthenticatedEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        repairRequest.setUser(user);
        repairService.saveRepairRequest(repairRequest);
        return "redirect:/repairs";
    }


    private String extractEmail(Map<String, Object> attributes) {
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        } else {
            return (String) attributes.get("email");
        }
    }


    @GetMapping
    public String showRepairList(Model model) {
        String email = extractAuthenticatedEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        List<RepairRequest> repairs = repairService.getRepairsByUserEmail(email);
        model.addAttribute("repairs", repairs);
        model.addAttribute("recentRepair", repairs.isEmpty() ? null : repairs.get(0));
        model.addAttribute("openRepairsCount", repairs.stream().filter(r -> !"완료".equals(r.getStatus())).count());
        return "repair/list";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        String email = extractAuthenticatedEmail();
        RepairRequest repair = repairService.getRepairById(id);

        if (!email.equals(repair.getStudentEmail())) {
            throw new IllegalArgumentException("본인의 요청만 수정할 수 있습니다.");
        }

        model.addAttribute("repairRequest", repair);
        return "repair/edit";
    }


    @PostMapping("/{id}/edit")
    public String updateRepair(@PathVariable Long id,
                               @ModelAttribute RepairRequest updatedRequest) {
        String email = extractAuthenticatedEmail();
        RepairRequest existing = repairService.getRepairById(id);

        if (!email.equals(existing.getStudentEmail())) {
            throw new IllegalArgumentException("본인의 요청만 수정할 수 있습니다.");
        }

        existing.setCategory(updatedRequest.getCategory());
        existing.setDetailType(updatedRequest.getDetailType());
        existing.setDescription(updatedRequest.getDescription());
        existing.setManager(updatedRequest.getManager());
        existing.setCmosPassword(updatedRequest.getCmosPassword());
        existing.setWindowsPassword(updatedRequest.getWindowsPassword());

        repairService.saveRepairRequest(existing);
        return "redirect:/repairs";
    }

    @PostMapping("/delete/{id}")
    public String deleteRepair(@PathVariable Long id) {
        String email = extractAuthenticatedEmail();
        repairService.deleteRepair(id, email);
        return "redirect:/repairs";
    }
}