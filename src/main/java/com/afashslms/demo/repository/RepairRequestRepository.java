package com.afashslms.demo.repository;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, String> {
    List<RepairRequest> findByUserOrderByCreatedAtDesc(User user);
}