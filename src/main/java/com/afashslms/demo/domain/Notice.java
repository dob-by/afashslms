package com.afashslms.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "MEDIUMTEXT ")
    private String content;

    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;  // 글 작성자 (MID_ADMIN or TOP_ADMIN)

    // 파일 업로드 관련 필드
    private String originalFileName;  // 사용자 업로드한 이름
    private String storedFileName;    // 서버에 저장된 실제 파일명
}