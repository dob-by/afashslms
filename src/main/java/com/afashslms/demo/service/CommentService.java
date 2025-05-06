package com.afashslms.demo.service;

import com.afashslms.demo.domain.Comment;
import com.afashslms.demo.domain.Post;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.CommentRepository;
import com.afashslms.demo.repository.PostRepository;
import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void addComment(String postId, String content, String email) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findByEmail(email).orElseThrow();

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public List<Comment> getCommentsForPost(String postId) {
        return commentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId);
    }

    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findByPost_PostIdOrderByCreatedAtAsc(postId);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElseThrow();
    }

    public void updateComment(Long id, String content, String email) {
        Comment comment = commentRepository.findById(id).orElseThrow();

        if (!comment.getUser().getEmail().equals(email)) {
            throw new RuntimeException("수정 권한 없음");
        }

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, String currentUserEmail) {
        Comment comment = getCommentById(commentId);
        String commentAuthorEmail = comment.getUser().getEmail();
        String role = comment.getUser().getRole().name(); // "USER", "ADMIN", 등

        if (!currentUserEmail.equals(commentAuthorEmail) && !role.equals("ADMIN")) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }


}