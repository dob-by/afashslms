package com.afashslms.demo.service;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.dto.LaptopViewDto;
import java.util.List;
import java.util.Optional;

public interface LaptopService {
    List<LaptopViewDto> getAllLaptopsForAdmin();
    Optional<Laptop> findById(String deviceId);
}
