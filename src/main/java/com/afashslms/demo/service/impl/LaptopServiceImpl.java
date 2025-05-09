package com.afashslms.demo.service.impl;

import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.dto.LaptopViewDto;
import com.afashslms.demo.repository.LaptopRepository;
import com.afashslms.demo.service.LaptopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaptopServiceImpl implements LaptopService {

    private final LaptopRepository laptopRepository;

    @Override
    public List<LaptopViewDto> getAllLaptopsForAdmin() {
        List<Laptop> laptops = laptopRepository.findAll(); // 필요시 fetch join
        return laptops.stream()
                .map(LaptopViewDto::fromEntity)
                .collect(Collectors.toList());
    }
}