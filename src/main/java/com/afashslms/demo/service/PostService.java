package com.afashslms.demo.service;

import com.afashslms.demo.domain.Post;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.PostRepository;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void createPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                           String title, String content) {

        User user = userDetails.getUser();

        Post post = new Post();
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());

        postRepository.save(post);
    }

    public void createPostWithFile(User user,
                                   String title,
                                   String content,
                                   MultipartFile file) {

        String savedFileName = null;
        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                savedFileName = UUID.randomUUID().toString() + extension;

                Path uploadDir = Paths.get("uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Path filePath = uploadDir.resolve(savedFileName);
                file.transferTo(filePath.toFile());

            } catch (Exception e) {
                throw new RuntimeException("파일 업로드 실패", e);
            }
        }

        Post post = new Post();
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());
        post.setFileName(savedFileName);

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

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> searchPostsByTitle(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }
}