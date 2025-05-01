package com.afashslms.demo.repository;

import com.afashslms.demo.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, String> {

    // 최신순 정렬 조회
    List<Post> findAllByOrderByCreatedAtDesc();

    // 특정 유저가 작성한 글 목록 (선택)
    List<Post> findByUser_UserId(String userId);
}