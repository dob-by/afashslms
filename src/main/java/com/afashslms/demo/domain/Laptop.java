package com.afashslms.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC) //테스트: public(더미데이터 사용을 위해), 실제: protected
@AllArgsConstructor
@Builder
public class Laptop {

    @Id
    @Column(name = "device_id")
    private String deviceId; // 일련번호, 예: LPT-2024-001

    private String modelName;

    private String ip;

    @Enumerated(EnumType.STRING)
    private LaptopStatus status; // 예: 수리중, 보유중 등

    private String currentState; // 간단한 문자열 상태 (예: 사용 중, 반납됨 등)

    private Integer manageNumber; // 관리번호, 예: 1, 2, 3 (String 아님에 주의)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_id"), nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user; // 노트북 소유자

    // 기타 생성일, 수정일 등 추가 가능
}
