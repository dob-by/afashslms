package com.afashslms.demo.repository;

import com.afashslms.demo.domain.Laptop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaptopRepository extends JpaRepository<Laptop, String> {
    // deviceId가 PK인 경우
}
