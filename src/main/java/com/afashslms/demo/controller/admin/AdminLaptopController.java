package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.*;
import com.afashslms.demo.dto.LaptopSearchConditionDto;
import com.afashslms.demo.dto.LaptopViewDto;
import com.afashslms.demo.repository.LaptopRepository;
import com.afashslms.demo.repository.OwnershipHistoryRepository;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.service.LaptopService;
import com.afashslms.demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.afashslms.demo.security.CustomUserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminLaptopController {

    private final UserService userService;
    private final LaptopService laptopService;
    private final LaptopRepository laptopRepository;
    private final OwnershipHistoryRepository ownershipHistoryRepository;

    @GetMapping("/admin/laptops")
    public String showLaptopList(@ModelAttribute("searchCond") LaptopSearchConditionDto searchCond,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam(defaultValue = "0") int page,            // ← 페이지 번호
                                 @RequestParam(defaultValue = "10") int size,           // ← 페이지당 항목 수
                                 Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<LaptopViewDto> laptopPage = laptopService.searchLaptops(searchCond, pageable);

        model.addAttribute("laptops", laptopPage);
        model.addAttribute("currentPage", laptopPage.getNumber());
        model.addAttribute("totalPages", laptopPage.getTotalPages());
        model.addAttribute("totalItems", laptopPage.getTotalElements());
        model.addAttribute("searchCond", searchCond);

        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUser().getUsername());
            model.addAttribute("userRole", userDetails.getRole().name());
        }

        return "admin/laptop-list";
    }

    @GetMapping("/admin/laptops/{deviceId}")
    @PreAuthorize("hasAnyRole('MID_ADMIN', 'TOP_ADMIN')")
    public String laptopDetail(@PathVariable String deviceId,
                               @AuthenticationPrincipal Object principal,
                               @RequestParam(required = false) String prevPage,
                               Model model,
                               @RequestParam(required = false) Integer page,
                               @RequestParam(required = false) String keyword,
                               HttpServletRequest request) throws AccessDeniedException {

        User user = null;
        String username = null;
        String role = null;
        String referer = request.getHeader("Referer");
        model.addAttribute("prevPage", prevPage != null ? prevPage : "/admin/laptops"); // 기본값 설정model.addAttribute("currentPage", page);
        model.addAttribute("currentPage", page);
        model.addAttribute("searchCond", new LaptopSearchConditionDto(keyword)); // 생성자에서 keyword 설정


        if (principal instanceof CustomUserDetails customUserDetails) {
            user = customUserDetails.getUser();
        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
            user = customOAuth2User.getUser();
            if (user == null) {
                throw new AccessDeniedException("OAuth2 로그인한 관리자의 정보가 누락되었습니다.");
            }
        } else {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        username = user.getUsername();
        role = user.getRole().name();

        Laptop laptop = laptopService.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("노트북을 찾을 수 없습니다."));
        List<User> users = userService.getAllUsers();
        List<OwnershipHistory> ownershipHistoryList = ownershipHistoryRepository.findByLaptop_DeviceId(deviceId);

        ObjectMapper objectMapper = new ObjectMapper();
        String usersJson = "";
        try {
            usersJson = objectMapper.writeValueAsString(users);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        model.addAttribute("laptop", laptop);
        model.addAttribute("users", users);
        model.addAttribute("usersJson", usersJson);
        model.addAttribute("ownershipHistoryList", ownershipHistoryList);
        model.addAttribute("username", username);
        model.addAttribute("userRole", role);

        return "admin/laptop-detail";
    }

    @PostMapping("/admin/laptops/updateStatus")
    @PreAuthorize("hasRole('TOP_ADMIN')")
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

    @GetMapping("/admin/laptops/{deviceId}/ownership")
    @PreAuthorize("hasAnyRole('MID_ADMIN', 'TOP_ADMIN')")
    public String showOwnershipHistory(@PathVariable String deviceId,
                                       @AuthenticationPrincipal Object principal,
                                       Model model)  throws AccessDeniedException{
        User user = null;
        String username = null;
        String role = null;

        if (principal instanceof CustomUserDetails customUserDetails) {
            user = customUserDetails.getUser();
        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
            user = customOAuth2User.getUser();
            if (user == null) {
                throw new AccessDeniedException("OAuth2 로그인한 관리자의 정보가 누락되었습니다.");
            }
        } else {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        username = user.getUsername();
        role = user.getRole().name();

        Laptop laptop = laptopService.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("노트북을 찾을 수 없습니다."));
        List<OwnershipHistory> ownershipHistoryList = ownershipHistoryRepository.findByLaptop_DeviceId(deviceId);

        model.addAttribute("laptop", laptop);
        model.addAttribute("ownershipHistoryList", ownershipHistoryList);
        model.addAttribute("username", username);
        model.addAttribute("userRole", role);

        return "admin/ownership-history"; // 너의 템플릿 파일명
    }

    @PostMapping("/admin/laptops/{deviceId}/change-owner")
    @PreAuthorize("hasRole('TOP_ADMIN')")
    public String changeOwner(@PathVariable String deviceId,
                              @RequestParam String newOwnerId,
                              @AuthenticationPrincipal CustomUserDetails loginUser,
                              HttpServletRequest request) throws AccessDeniedException {

        if (loginUser == null || loginUser.getRole() != Role.TOP_ADMIN) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 💻 실제 소유자 변경 처리
        laptopService.changeLaptopOwner(deviceId, newOwnerId);

        // 🔙 Referer를 받아서 이전 페이지 주소로 활용
        String referer = request.getHeader("Referer");
        String encodedPrevPage = (referer != null)
                ? URLEncoder.encode(referer, StandardCharsets.UTF_8)
                : URLEncoder.encode("/admin/laptops", StandardCharsets.UTF_8);

        return "redirect:/admin/laptops/" + deviceId + "?prevPage=" + encodedPrevPage;
    }

}