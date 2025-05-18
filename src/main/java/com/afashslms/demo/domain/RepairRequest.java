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

    private String deviceSerial;        // 기기 번호
    private String category;            // HW or SW
    private String detailType;          // 구체적 문제 유형
    private String description;         // 상세 사유
    private String cmosPassword;
    private String windowsPassword;
    private String manager;            // 담당자

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairStatus status;       // 상태: REQUESTED, COMPLETED 등

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = RepairStatus.REQUESTED;
    }

    public String getStudentEmail() {
        return user != null ? user.getEmail() : null;
    }
}