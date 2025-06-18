package com.afashslms.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupFormDto {

    @NotBlank(message = "아이디는 필수입니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String username;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일 입력은 필수입니다.")
    private String email;

    @NotBlank(message = "소속 선택은 필수입니다.")
    private String affiliation; //학생대, 교육대

    @NotBlank(message = "중대 선택은 필수입니다.")
    private String unit; //1~3중대
}