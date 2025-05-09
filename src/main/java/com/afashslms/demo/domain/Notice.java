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
    private String content;

    private Timestamp createdAt;

    @ManyToOne
    private User author;  // 글 작성자 (MID_ADMIN or TOP_ADMIN)
}