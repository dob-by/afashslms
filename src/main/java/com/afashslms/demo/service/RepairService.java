package com.afashslms.demo.service;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.RepairRequestRepository;
import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRequestRepository repairRequestRepository;
    private final UserRepository userRepository;

    public List<RepairRequest> getRepairsByUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        return repairRequestRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void saveRepairRequest(RepairRequest request) {
        repairRequestRepository.save(request);
    }
}