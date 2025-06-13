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
    private String userId;

    public static LaptopViewDto fromEntity(Laptop laptop) {
        return LaptopViewDto.builder()
                .modelName(laptop.getModelName())
                .deviceId(laptop.getDeviceId())
                .ip(laptop.getIp())
                .status(laptop.getStatus().getDisplayName())
                .userName(laptop.getUser() != null ? laptop.getUser().getUsername() : "미지정")
                .currentState(laptop.getCurrentState())
                .manageCode(String.format("%03d", laptop.getManageNumber()))
                .build();
    }

    private LaptopViewDto convertToViewDto(Laptop laptop) {
        return LaptopViewDto.builder()
                .deviceId(laptop.getDeviceId())
                .modelName(laptop.getModelName())
                .ip(laptop.getIp())
                .status(laptop.getStatus().name())
                .currentState(laptop.getCurrentState())
                .manageCode(String.format("%03d", laptop.getManageNumber())) // ✔ 문자열 코드 유지
                .userName(laptop.getUser() != null ? laptop.getUser().getUsername() : null)
                .userId(laptop.getUser() != null ? laptop.getUser().getUserId() : null)
                .build();
    }
}