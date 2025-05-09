package com.afashslms.demo.domain;

public enum LaptopStatus {
    IN_USE("사용 중"),
    IN_REPAIR("수리 중"),
    REPAIR_REQUESTED("수리 요청됨"),
    AVAILABLE("대기 중");

    private final String displayName;

    LaptopStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
