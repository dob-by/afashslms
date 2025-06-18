package com.afashslms.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC) // TODO: 엑셀 업로드 마무리 후 Builder로 전환 예정
@AllArgsConstructor
@Builder
public class Laptop {

    @Id
    @Column(name = "device_id")
    private String deviceId; // 일련번호

    private String modelName;

    private String ip;

    @Enumerated(EnumType.STRING)
    private LaptopStatus status; // 수리중, 보유중 등

    private String currentState; // 사용 중, 반납됨 등

    private Integer manageNumber; // 관리번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_id"), nullable = true)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user; // 노트북 소유자

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @ManyToOne
    @JoinColumn(name = "current_owner_id")
    private User currentOwner;

}
