package com.afashslms.demo.repository;

import com.afashslms.demo.domain.OwnershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnershipHistoryRepository extends JpaRepository<OwnershipHistory, Long> {
    List<OwnershipHistory> findByLaptop_DeviceId(String deviceId);
}
