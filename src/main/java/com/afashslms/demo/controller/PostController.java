package com.afashslms.demo.controller;

import com.afashslms.demo.domain.Post;
import com.afashslms.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 게시글 목록
    @GetMapping
    public String listPosts(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return "post/list";  // templates/post/list.html
    }

    // 게시글 상세
    @GetMapping("/{postId}")
    public String viewPost(@PathVariable String postId, Model model) {
        postService.incrementViewCount(postId); // 👈 조회수 증가 먼저
        Post post = postService.getPost(postId);
        model.addAttribute("post", post);
        return "post/view"; // templates/post/view.html
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
}