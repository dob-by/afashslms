package com.afashslms.demo.repository;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.User;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    List<RepairRequest> findByUserOrderByCreatedAtDesc(User user);
    List<RepairRequest> findAllByUser_EmailOrderByCreatedAtDesc(String email);
}