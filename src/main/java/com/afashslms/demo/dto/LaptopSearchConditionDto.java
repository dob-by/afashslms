package com.afashslms.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaptopSearchConditionDto {
    private String modelName;
    private String deviceId;
    private String username;
    private String status;
    private String keyword;

    public LaptopSearchConditionDto() {}

    public LaptopSearchConditionDto(String keyword) {
        this.keyword = keyword;
    }
}