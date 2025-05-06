package com.afashslms.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id")
    private String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // 작성자
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String author; // 작성자 이름 or ID

    @CreationTimestamp
    private LocalDateTime createdAt;

    private int viewCount = 0;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}