package com.afashslms.demo.service.impl;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.OwnershipHistory;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.dto.LaptopSearchConditionDto;
import com.afashslms.demo.dto.LaptopViewDto;
import com.afashslms.demo.repository.LaptopRepository;
import com.afashslms.demo.repository.OwnershipHistoryRepository;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.service.LaptopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.Optional;

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
        List<Laptop> laptops = laptopRepository.findAll();
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

    @Override
    public Page<LaptopViewDto> searchLaptops(LaptopSearchConditionDto condition, Pageable pageable) {
        List<Laptop> all = laptopRepository.findAll();

        // 필터링
        List<Laptop> filtered = all.stream()
                .filter(laptop -> {
                    String keyword = condition.getKeyword();
                    return keyword == null || keyword.isBlank()
                            || laptop.getDeviceId().contains(keyword)
                            || laptop.getModelName().contains(keyword)
                            || (laptop.getUser() != null && laptop.getUser().getUsername().contains(keyword))
                            || laptop.getStatus().getDisplayName().contains(keyword)
                            || (laptop.getIp() != null && laptop.getIp().contains(keyword))
                            || (laptop.getCurrentState() != null && laptop.getCurrentState().contains(keyword))
                            || String.format("%03d", laptop.getManageNumber()).contains(keyword);
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<LaptopViewDto> content = filtered.subList(start, end)
                .stream()
                .map(LaptopViewDto::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, filtered.size());
    }


    @Override
    public List<LaptopViewDto> searchLaptops(LaptopSearchConditionDto condition) {
        return laptopRepository.findAll().stream()
                .filter(laptop -> {
                    String keyword = condition.getKeyword();
                    return keyword == null || keyword.isBlank() ||
                            laptop.getDeviceId().contains(keyword) ||
                            laptop.getModelName().contains(keyword) ||
                            (laptop.getUser() != null && laptop.getUser().getUsername().contains(keyword));
                })
                .map(LaptopViewDto::fromEntity)
                .toList();
    }
}