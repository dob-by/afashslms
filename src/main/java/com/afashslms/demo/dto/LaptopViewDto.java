package com.afashslms.demo.dto;


import com.afashslms.demo.domain.Laptop;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LaptopViewDto {

    private String modelName;
    private String deviceId;
    private String ip;
    private String status; // 수리중 등
    private String userName; // 연관된 User 이름
    private String currentState; // 현재상태
    private String manageCode; // 001, 002...

    public static LaptopViewDto fromEntity(Laptop laptop) {
        return LaptopViewDto.builder()
                .modelName(laptop.getModelName())
                .deviceId(laptop.getDeviceId())
                .ip(laptop.getIp())
                .status(laptop.getStatus().getDisplayName()) // enum이면 getDisplayName() 등
                .userName(laptop.getUser() != null ? laptop.getUser().getUsername() : "미지정")
                .currentState(laptop.getCurrentState())
                .manageCode(String.format("%03d", laptop.getManageNumber()))
                .build();
    }
}