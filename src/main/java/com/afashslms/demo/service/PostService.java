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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
            String email = null;

            // 우선 일반적인 email 속성에서 시도
            Object emailAttr = oauthToken.getPrincipal().getAttribute("email");

            // Kakao일 경우 email이 nested 구조임
            if (emailAttr == null) {
                Object kakaoAccount = oauthToken.getPrincipal().getAttribute("kakao_account");
                if (kakaoAccount instanceof Map<?, ?> kakaoMap) {
                    email = (String) kakaoMap.get("email");
                }
            } else {
                email = (String) emailAttr;
            }

            System.out.println(">> OAuth2 email: " + email);
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("OAuth 사용자 정보를 찾을 수 없습니다."));

        } else {
            // Local 로그인
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

    @Transactional
    public void incrementViewCount(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.setViewCount(post.getViewCount() + 1);
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

    public String findRoleByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRole().name())
                .orElse("USER");
    }
}