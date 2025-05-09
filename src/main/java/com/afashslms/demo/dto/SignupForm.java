package com.afashslms.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupForm {

    @NotBlank(message = "아이디는 필수입니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "가군번은 필수입니다.")
    private String militaryId;

    @NotBlank(message = "이름은 필수입니다.")
    private String username;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "가입자 유형 선택은 필수입니다.")
    private String role; // "STUDENT" 또는 "MID_ADMIN"
}