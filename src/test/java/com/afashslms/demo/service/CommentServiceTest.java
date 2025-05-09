package com.afashslms.demo.service;

import com.afashslms.demo.domain.Comment;
import com.afashslms.demo.domain.Post;
import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.CommentRepository;
import com.afashslms.demo.repository.PostRepository;
import com.afashslms.demo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    private Post post;
    private User user;
    private User admin;
    private User user2;

    @Autowired
    EntityManager em;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId("user1");
        user.setEmail("user@test.com");
        user.setRole(Role.STUDENT);
        userRepository.save(user);

        user2 = new User();
        user2.setUserId("user2");
        user2.setEmail("other@test.com");
        user2.setRole(Role.STUDENT);
        userRepository.save(user2);

        // admin 계정 중복 체크
        admin = userRepository.findByEmail("admin@test.com").orElseGet(() -> {
            User newAdmin = new User();
            newAdmin.setUserId("admin1");
            newAdmin.setEmail("admin@test.com");
            newAdmin.setRole(Role.TOP_ADMIN);
            newAdmin.setUsername("관리자");
            return userRepository.save(newAdmin);
        });

        post = new Post();
        post.setTitle("테스트용 게시글");
        post.setContent("본문입니다");
        post.setUser(user);
        postRepository.save(post);

        em.flush();
        em.clear();

        post = postRepository.findById(post.getPostId()).orElseThrow(); // 이제 진짜 저장되어 있어야 함!
    }

    @Test
    void 댓글_작성_성공() {
        Post refreshedPost = postRepository.findById(post.getPostId()).orElseThrow();
        commentService.addComment(refreshedPost.getPostId(), "댓글 내용", user.getEmail());

        List<Comment> comments = commentRepository.findByPost_PostIdOrderByCreatedAtAsc(refreshedPost.getPostId());

        assertEquals(1, comments.size());
        assertEquals("댓글 내용", comments.get(0).getContent());
        assertEquals(user.getEmail(), comments.get(0).getUser().getEmail());
    }

    @Test
    void 댓글_작성자가_수정_성공() {
        commentService.addComment(post.getPostId(), "원본 댓글", user.getEmail());
        Comment saved = commentRepository.findAll().get(0);

        commentService.updateComment(saved.getId(), "수정된 댓글", user.getEmail());

        Comment updated = commentRepository.findById(saved.getId()).orElseThrow();
        assertEquals("수정된 댓글", updated.getContent());
    }


    @Test
    void 댓글_작성자가_아닌_사람이_수정시_예외() {
        commentService.addComment(post.getPostId(), "댓글", user.getEmail());
        Comment saved = commentRepository.findAll().get(0);

        assertThrows(RuntimeException.class, () -> {
            commentService.updateComment(saved.getId(), "수정!", "other@test.com");
        });
    }


    @Test
    void 댓글_작성자_삭제_성공() {
        commentService.addComment(post.getPostId(), "삭제할 댓글", user.getEmail());
        Comment saved = commentRepository.findAll().get(0);

        commentService.deleteComment(saved.getId(), user.getEmail());

        assertFalse(commentRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void 권한_없는_유저_댓글_삭제_실패() {
        commentService.addComment(post.getPostId(), "삭제할 댓글", user.getEmail());
        Comment saved = commentRepository.findAll().get(0);

        assertThrows(RuntimeException.class, () -> {
            commentService.deleteComment(saved.getId(), user2.getEmail());
        });
    }
}