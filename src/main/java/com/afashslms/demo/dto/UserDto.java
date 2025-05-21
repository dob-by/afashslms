package com.afashslms.demo.dto;

import com.afashslms.demo.domain.User;
import lombok.Getter;


@Getter
public class UserDto {
    private String userId;
    private String username;

    public UserDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
    }
}
