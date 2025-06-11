package com.afashslms.demo.domain;

public enum Role {
    STUDENT("학생"),
    MID_ADMIN("중간 관리자"),
    TOP_ADMIN("총괄 관리자"),
    PENDING_ADMIN("승인 대기 관리자");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAdmin() {
        return this == MID_ADMIN || this == TOP_ADMIN;
    }
}