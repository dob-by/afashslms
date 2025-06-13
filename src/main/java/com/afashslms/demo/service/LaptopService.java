package com.afashslms.demo.service;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.dto.LaptopSearchConditionDto;
import com.afashslms.demo.dto.LaptopViewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LaptopService {
    List<LaptopViewDto> getAllLaptopsForAdmin();
    Optional<Laptop> findById(String deviceId);
    void changeLaptopOwner(String deviceId, String newOwnerId);
    Laptop findCurrentLaptopByEmail(String email);
    List<LaptopViewDto> searchLaptops(LaptopSearchConditionDto condition);
    Page<LaptopViewDto> searchLaptops(LaptopSearchConditionDto condition, Pageable pageable);
}
