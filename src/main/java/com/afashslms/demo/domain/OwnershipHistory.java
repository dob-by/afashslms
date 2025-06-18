package com.afashslms.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OwnershipHistory {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Laptop laptop;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; //이전사용자

    private LocalDateTime changedAt;

    @ManyToOne
    @JoinColumn(name = "current_owner_id")
    private User currentOwner;
}
