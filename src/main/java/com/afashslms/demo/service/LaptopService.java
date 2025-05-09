package com.afashslms.demo.service;

import com.afashslms.demo.dto.LaptopViewDto;
import java.util.List;

public interface LaptopService {
    List<LaptopViewDto> getAllLaptopsForAdmin();
}
