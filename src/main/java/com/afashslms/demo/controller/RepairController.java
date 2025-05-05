package com.afashslms.demo.controller;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
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

    @GetMapping("/new")
    public String showRepairForm() {
        return "repair/new";  // templates/repair/new.html
    }

    // ✅ 수리 요청서 제출 처리
    @PostMapping
    public String submitRepair(@ModelAttribute RepairRequest repairRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            // 소셜 로그인 (Google, Kakao)
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
            email = extractEmail(attributes);
        } else {
            // 로컬 로그인
            Object principalObj = authentication.getPrincipal();

            if (principalObj instanceof UserDetails userDetails) {
                email = userDetails.getUsername();  // 이메일로 저장되도록 설정해둔 경우
            } else {
                throw new IllegalStateException("로그인 사용자 정보를 가져올 수 없습니다.");
            }
        }

        // 공통 처리
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
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
    public String showRepairList(Model model,
                                 @AuthenticationPrincipal Object principal) {
        String email = null;

        if (principal instanceof OAuth2User oAuth2User) {
            // 소셜 로그인 (Google, Kakao)
            Map<String, Object> attributes = oAuth2User.getAttributes();

            if (attributes.containsKey("kakao_account")) {
                // Kakao
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                email = (String) kakaoAccount.get("email");
            } else {
                // Google
                email = (String) attributes.get("email");
            }
        } else if (principal instanceof org.springframework.security.core.userdetails.User user) {
            // Local 로그인
            email = user.getUsername(); // UserDetails에서 getUsername() == email

        }

        System.out.println("현재 로그인한 이메일: " + email);

        if (email == null) {
            throw new RuntimeException("로그인된 사용자의 이메일 정보를 가져올 수 없습니다.");
        }

        User userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        List<RepairRequest> repairs = repairService.getRepairsByUserEmail(userEntity.getEmail());
        RepairRequest recentRepair = repairs.isEmpty() ? null : repairs.get(0);
        long openRepairsCount = repairs.stream().filter(r -> !"완료".equals(r.getStatus())).count();

        model.addAttribute("repairs", repairs);
        model.addAttribute("recentRepair", recentRepair);
        model.addAttribute("openRepairsCount", openRepairsCount);
        return "repair/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        RepairRequest repair = repairService.getRepairById(id);

        // 로그인한 사용자와 요청 작성자가 같은지 확인 (보안!)
        if (!repair.getUser().getUserId().equals(principal.getName())) {
            throw new IllegalArgumentException("본인의 요청만 수정할 수 있습니다.");
        }

        model.addAttribute("repairRequest", repair);
        return "repair/edit";  // 수정 폼으로 이동
    }

    @PostMapping("/edit/{id}")
    public String updateRepair(@PathVariable Long id, @ModelAttribute RepairRequest updatedRequest, Principal principal) {
        repairService.updateRepair(id, updatedRequest, principal.getName());
        return "redirect:/repairs";
    }

    @PostMapping("/delete/{id}")
    public String deleteRepair(@PathVariable Long id, Principal principal) {
        repairService.deleteRepair(id, principal.getName());
        return "redirect:/repairs";
    }
}