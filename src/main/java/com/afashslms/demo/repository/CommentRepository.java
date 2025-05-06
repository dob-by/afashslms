package com.afashslms.demo.repository;

import com.afashslms.demo.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(String postId);
}
