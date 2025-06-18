package com.afashslms.demo.service;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.OwnershipHistory;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.LaptopRepository;
import com.afashslms.demo.repository.OwnershipHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final OwnershipHistoryRepository ownershipHistoryRepository;
    private final LaptopRepository laptopRepository;

    public void changeOwnership(Laptop laptop, User newOwner) {
        // 기존 소유자 가져오기
        User previousOwner = laptop.getCurrentOwner();

        // 이전 소유자 기록 저장
        OwnershipHistory history = new OwnershipHistory();
        history.setLaptop(laptop);
        history.setUser(previousOwner);       // 이전 사용자
        history.setCurrentOwner(newOwner);    // 새 사용자
        history.setChangedAt(LocalDateTime.now());

        ownershipHistoryRepository.save(history);

        // 노트북 현재 소유자 갱신
        laptop.setCurrentOwner(newOwner);
        laptopRepository.save(laptop);
    }
}