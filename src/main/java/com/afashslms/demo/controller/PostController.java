package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Post;
import com.afashslms.demo.security.CustomOAuth2User;
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
                           HttpServletRequest request,
                           @AuthenticationPrincipal CustomOAuth2User principal) {

        postService.incrementViewCount(postId);
        Post post = postService.getPost(postId);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.getCommentsByPostId(postId));

        // ✅ 로그인 사용자 정보 추가
        if (principal != null) {
            model.addAttribute("currentUserEmail", principal.getEmail());
            model.addAttribute("userRole", principal.getRole().name()); // 'USER', 'ADMIN' 등
        } else {
            model.addAttribute("currentUserEmail", null);
            model.addAttribute("userRole", "ANONYMOUS");
        }

        // CSRF 토큰도 그대로 유지
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);

        return "post/view";
    }

    // 글쓰기 폼
    @GetMapping("/new")
    public String showCreateForm() {
        return "post/new"; // templates/post/new.html
    }

    @GetMapping("/post/new")
    public String showPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "post/form"; // templates/post/form.html
    }

    // 글 등록 처리
    @PostMapping
    public String createPost(@RequestParam String title,
                             @RequestParam String content,
                             Principal principal) {
        System.out.println(">> Principal.getName(): " + principal.getName());
        String userId = principal.getName();  // 로그인된 사용자 ID
        postService.createPost(userId, title, content);
        return "redirect:/posts";
    }

    // 글 수정 폼
    @GetMapping("/{postId}/edit")
    public String showEditForm(@PathVariable String postId, Model model) {
        Post post = postService.getPost(postId);
        model.addAttribute("post", post);
        return "post/edit";  // templates/post/edit.html
    }

    // 글 수정 처리
    @PostMapping("/{postId}/edit")
    public String updatePost(@PathVariable String postId,
                             @RequestParam String title,
                             @RequestParam String content) {
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