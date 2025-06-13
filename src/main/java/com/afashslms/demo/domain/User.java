package com.afashslms.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.sql.Timestamp;
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

    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;  //STUDENT, MID_ADMIN, TOP_ADMIN, PENDING_ADMIN

    private Timestamp createdAt;

    @Column(name = "military_id", unique = true)
    private String militaryId; //가군번

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Laptop> laptops = new ArrayList<>();

    @Column(name = "birth")
    private String birth;

    private String affiliation; // "학생대" or "교육대"

    private String unit; // "1중대", "2중대", "3중대" or null

    @Column(name = "profile_completed")
    private Boolean profileCompleted = false;

    public boolean isProfileComplete() {
        return Boolean.TRUE.equals(this.profileCompleted);
    }

    @Column(nullable = false)
    private boolean passwordChanged = false;
}
