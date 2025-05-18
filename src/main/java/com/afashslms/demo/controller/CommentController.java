package com.afashslms.demo.controller;

import com.afashslms.demo.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

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

    @GetMapping("/{id}/edit")
    public String showEditCommentForm(@PathVariable Long id,
                                      Model model,
                                      Principal principal,
                                      HttpServletRequest request) {
        String email = extractEmailFromPrincipal(principal);
        var comment = commentService.getCommentById(id);

        // 본인이 작성한 댓글인지 확인
        if (!comment.getUser().getEmail().equals(email)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("comment", comment);

        return "comment/edit";
    }

    @PostMapping("/{id}/edit")
    @ResponseBody
    public String editComment(@PathVariable Long id,
                              @RequestBody Map<String, String> body,
                              Principal principal) {

        String email = extractEmailFromPrincipal(principal);
        String content = body.get("content");

        commentService.updateComment(id, content, email);

        return content; // 인라인 수정에서는 새 content를 반환
    }




    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id, Principal principal) {
        String email = extractEmailFromPrincipal(principal);

        String postId = commentService.getCommentById(id).getPost().getPostId();

        commentService.deleteComment(id, email);

        return "redirect:/posts/" + postId;
    }
}