package com.afashslms.demo.domain;

public enum RepairStatus {
    REQUESTED("수리 요청됨"),
    IN_PROGRESS("수리 중"),
    COMPLETED("수리 완료"),
    REJECTED("반려");

    private final String displayName;

    RepairStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}