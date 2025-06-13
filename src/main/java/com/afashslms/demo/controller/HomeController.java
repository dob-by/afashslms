package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Post;
import com.afashslms.demo.repository.PostRepository;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.NoticeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import com.afashslms.demo.domain.Notice;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final PostRepository postRepository;
    private final NoticeService noticeService;

    public HomeController(PostRepository postRepository, NoticeService noticeService) {
        this.postRepository = postRepository;
        this.noticeService = noticeService;
    }

    @GetMapping("/")
    public String showHome(Model model, Authentication authentication) {
        String roleName = "GUEST"; // 기본값
        String username = "비회원";

        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            roleName = oAuth2User.getUser().getRole().name(); // STUDENT 등
            username = oAuth2User.getUser().getUsername();
        } else if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            roleName = userDetails.getUser().getRole().name();
            username = userDetails.getUser().getUsername();
        }

        model.addAttribute("userRole", roleName);
        model.addAttribute("username", username);

        // 최신 게시글 5개
        List<Post> latestPosts = postRepository.findTop5ByOrderByCreatedAtDesc();
        model.addAttribute("latestPosts", latestPosts);

        // 최신 공지사항 5개
        List<Notice> latestNotices = noticeService.getLatestNotices(5);
        model.addAttribute("latestNotices", latestNotices);

        return "home";
    }
}
