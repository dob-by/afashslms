package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Post;
import com.afashslms.demo.repository.PostRepository;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final PostRepository postRepository;

    public HomeController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/")
    public String showHome(Model model, Authentication authentication) {
        String roleName = "GUEST"; // 기본값

        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User) {
            roleName = oAuth2User.getUser().getRole().name(); // STUDENT 등
        } else if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            roleName = userDetails.getUser().getRole().name();
        }

        model.addAttribute("userRole", roleName);

        // 최신 게시글 5개 가져오기
        List<Post> latestPosts = postRepository.findTop5ByOrderByCreatedAtDesc();
        model.addAttribute("latestPosts", latestPosts);

        return "home";
    }
}
