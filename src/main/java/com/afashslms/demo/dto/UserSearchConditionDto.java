package com.afashslms.demo.dto;

import lombok.Data;

@Data
public class UserSearchConditionDto {
    private String keyword; // 군번, 이름, 이메일에 대해 통합 검색
}