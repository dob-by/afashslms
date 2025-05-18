package com.afashslms.demo.service.impl;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.OwnershipHistory;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.dto.LaptopViewDto;
import com.afashslms.demo.repository.LaptopRepository;
import com.afashslms.demo.repository.OwnershipHistoryRepository;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.service.LaptopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaptopServiceImpl implements LaptopService {

    private final LaptopRepository laptopRepository;
    private final OwnershipHistoryRepository ownershipHistoryRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<Laptop> findById(String deviceId) {
        return laptopRepository.findById(deviceId);
    }

    @Override
    public List<LaptopViewDto> getAllLaptopsForAdmin() {
        List<Laptop> laptops = laptopRepository.findAll(); // 필요시 fetch join
        return laptops.stream()
                .map(LaptopViewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeLaptopOwner(String deviceId, User newOwner) {
        Laptop laptop = laptopRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 노트북 없음"));

        // 기존 소유자 저장
        User oldOwner = laptop.getUser();

        // 이력 기록
        if (oldOwner != null) {
            OwnershipHistory history = new OwnershipHistory();
            history.setLaptop(laptop);
            history.setUser(oldOwner);
            history.setChangedAt(LocalDateTime.now());
            ownershipHistoryRepository.save(history);
        }

        // 실제 소유자 변경
        laptop.setUser(newOwner);
        laptopRepository.save(laptop);
    }

    @Override
    public void changeLaptopOwner(String deviceId, String newOwnerId) {
        Laptop laptop = laptopRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("노트북을 찾을 수 없습니다."));
        User newOwner = userRepository.findByUserId(newOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이전 사용자 정보 이력에 저장
        if (laptop.getUser() != null) {
            OwnershipHistory history = new OwnershipHistory();
            history.setLaptop(laptop);
            history.setUser(laptop.getUser()); // 이전 사용자
            history.setChangedAt(LocalDateTime.now());
            ownershipHistoryRepository.save(history);
        }

        // 새 사용자로 변경
        laptop.setUser(newOwner);
        laptopRepository.save(laptop);
    }

    @Override
    public Laptop findCurrentLaptopByEmail(String email) {
        return laptopRepository.findTopByUser_EmailOrderByIssuedAtDesc(email)
                .orElse(null);
    }
}