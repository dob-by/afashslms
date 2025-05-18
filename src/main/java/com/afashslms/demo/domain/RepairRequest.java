package com.afashslms.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repair_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "email", name = "student_email")
    private User user;

    private String deviceSerial; // 기기 번호

    private String category; // HW or SW
    private String detailType; // 구체적 문제 유형
    private String description; // 상세 사유
    private String cmosPassword;
    private String windowsPassword;

    private String manager; // 담당자
    private String status; // 상태: 접수중, 완료 등

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "접수중";
    }

    public String getStudentEmail() {
        return user != null ? user.getEmail() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDetailType() {
        return detailType;
    }

    public void setDetailType(String detailType) {
        this.detailType = detailType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCmosPassword() {
        return cmosPassword;
    }

    public void setCmosPassword(String cmosPassword) {
        this.cmosPassword = cmosPassword;
    }

    public String getWindowsPassword() {
        return windowsPassword;
    }

    public void setWindowsPassword(String windowsPassword) {
        this.windowsPassword = windowsPassword;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}