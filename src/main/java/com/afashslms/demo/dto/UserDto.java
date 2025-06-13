package com.afashslms.demo.dto;

import com.afashslms.demo.domain.User;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UserDto {
    private String userId;
    private String username;
    private String email;
    private String role;
    private String militaryId;
    private LocalDateTime createdAt;

    public UserDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.militaryId = user.getMilitaryId();
        this.createdAt = user.getCreatedAt().toLocalDateTime();
    }

    public static UserDto fromEntity(User user) {
        return new UserDto(user);
    }
}