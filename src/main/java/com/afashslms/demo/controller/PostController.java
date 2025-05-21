package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Post;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import com.afashslms.demo.service.CommentService;
import com.afashslms.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "ANONYMOUS";
        }

        Object principal = authentication.getPrincipal();

        // CustomOAuth2User인 경우
        if (principal instanceof CustomOAuth2User user) {
            return user.getRole().name();  // <- 이 부분이 핵심!
        }

        return "ANONYMOUS";
    }

    private String extractEmailFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

            if (attributes.containsKey("kakao_account")) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                return (String) kakaoAccount.get("email");
            } else {
                return (String) attributes.get("email"); // Google
            }
        } else {
            return principal.getName(); // 로컬 로그인
        }
    }

    // 게시글 목록
    @GetMapping
    public String listPosts(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return "post/list";  // templates/post/list.html
    }

    // 게시글 상세
    @GetMapping("/{postId}")
    public String viewPost(@PathVariable String postId,
                           Model model,
                           HttpServletRequest request) {

        postService.incrementViewCount(postId);
        Post post = postService.getPost(postId);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.getCommentsByPostId(postId));

        // 현재 로그인한 사용자 정보를 가져오기 위한 인증 객체 획득
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자의 경우
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomOAuth2User user) {
                model.addAttribute("currentUserEmail", user.getEmail());
                model.addAttribute("userRole", user.getRole().name());

            } else if (principal instanceof CustomUserDetails userDetails) {
                model.addAttribute("currentUserEmail", userDetails.getEmail());
                model.addAttribute("userRole", userDetails.getRole().name());

            } else if (principal instanceof org.springframework.security.core.userdetails.User user) {
                String email = user.getUsername();
                String role = postService.findRoleByEmail(email);
                model.addAttribute("currentUserEmail", email);
                model.addAttribute("userRole", role);

            } else if (principal instanceof String email) {
                model.addAttribute("currentUserEmail", email);
                model.addAttribute("userRole", "USER");

            } else {
                model.addAttribute("currentUserEmail", null);
                model.addAttribute("userRole", "ANONYMOUS");
            }
        } else {
            model.addAttribute("currentUserEmail", null);
            model.addAttribute("userRole", "ANONYMOUS");
        }

        // CSRF
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);

        return "post/view";
    }

    @GetMapping("/new")
    public String showPostForm(Model model, @AuthenticationPrincipal Object principal) {
        String role = "ANONYMOUS";
        String username = "알 수 없음";

        if (principal instanceof CustomUserDetails userDetails) {
            role = userDetails.getUser().getRole().name();
            username = userDetails.getUser().getUsername();
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            role = oauthUser.getRole().name();
            username = oauthUser.getName();
        }

        // STUDENT만 접근 허용
        if (!"STUDENT".equals(role)) {
            return "post/forbidden";
        }

        model.addAttribute("userRole", role);
        model.addAttribute("username", username);
        return "post/new";
    }

    // 글 등록 처리
    @PostMapping
    public String createPost(@RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             @AuthenticationPrincipal Object principal) {

        String email = null;
        String role = null;

        if (principal instanceof CustomUserDetails userDetails) {
            email = userDetails.getUser().getEmail();
            role = userDetails.getUser().getRole().name();
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            email = oauthUser.getEmail();
            role = oauthUser.getRole().name();
        }

        if (!"STUDENT".equals(role)) {
            return "post/forbidden";
        }

        postService.createPostWithFile(email, title, content, file);  // ✨파일 포함 메서드 호출
        return "redirect:/posts";
    }
    // 글 수정 폼
    @GetMapping("/{postId}/edit")
    public String showEditForm(@PathVariable String postId, Model model, Principal principal) {
        Post post = postService.getPost(postId);
        String email = extractEmailFromPrincipal(principal);  // 현재 로그인한 사용자 이메일

        // 게시글 작성자 이메일과 로그인한 사용자 이메일 비교
        if (!post.getUser().getEmail().equals(email)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("post", post);
        return "post/edit";  // templates/post/edit.html
    }

    // 글 수정 처리
    @PostMapping("/{postId}/edit")
    public String updatePost(@PathVariable String postId,
                             @RequestParam String title,
                             @RequestParam String content,
                             Principal principal) {

        Post post = postService.getPost(postId);
        String email = extractEmailFromPrincipal(principal);

        if (!post.getUser().getEmail().equals(email)) {
            return "redirect:/access-denied";
        }

        postService.updatePost(postId, title, content);
        return "redirect:/posts/" + postId;
    }

    // 글 삭제
    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return "redirect:/posts";
    }

    //댓글 출력 및 작성 처리
    @PostMapping("/{postId}/comments")
    public String addComment(@PathVariable String postId,
                             @RequestParam String content,
                             Principal principal) {

        String email = extractEmailFromPrincipal(principal);
        commentService.addComment(postId, content, email);
        return "redirect:/posts/" + postId;
    }


}