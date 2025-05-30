package com.afashslms.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;  //PK

    private String password;

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String provider; //'local', 'kakao', 'google'

    private String oauthId; //소셜 로그인 식별자

    @Enumerated(EnumType.STRING)
    private Role role;  //STUDENT, MID_ADMIN, TOP_ADMIN

    private Timestamp createdAt;

    @Column(name = "military_id", unique = true)
    private String militaryId; //가군번

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Laptop> laptops = new ArrayList<>();
}
