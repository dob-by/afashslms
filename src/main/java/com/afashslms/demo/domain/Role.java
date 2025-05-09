package com.afashslms.demo.domain;

public enum Role {
    STUDENT, MID_ADMIN, TOP_ADMIN;

    public boolean isAdmin() {
        return this == MID_ADMIN || this == TOP_ADMIN;
    }
}
