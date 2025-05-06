package com.afashslms.demo.service;

import com.afashslms.demo.domain.Comment;
import com.afashslms.demo.domain.Post;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.CommentRepository;
import com.afashslms.demo.repository.PostRepository;
import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    // TODO: 수정/삭제 기능 추가
}