package com.afashslms.demo.service;

import com.afashslms.demo.domain.Post;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.PostRepository;
import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 등록
    public void createPost(String principalName, String title, String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user;

        System.out.println(">> Auth class: " + authentication.getClass().getName());
        System.out.println(">> Principal name: " + principalName);

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String email = oauthToken.getPrincipal().getAttribute("email");
            System.out.println(">> OAuth2 email: " + email);
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("OAuth 사용자 정보를 찾을 수 없습니다."));
        } else {
            user = userRepository.findByUserId(principalName)
                    .orElseThrow(() -> new IllegalArgumentException("Local 사용자 정보를 찾을 수 없습니다."));
        }

        Post post = new Post();
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());

        postRepository.save(post);
    }

    // 게시글 목록
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    // 게시글 단건 조회
    public Post getPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    // 게시글 수정
    public Post updatePost(String postId, String title, String content) {
        Post post = getPost(postId);
        post.setTitle(title);
        post.setContent(content);
        return postRepository.save(post);
    }

    // 게시글 삭제
    public void deletePost(String postId) {
        postRepository.deleteById(postId);
    }
}