package com.afashslms.demo.repository;

import com.afashslms.demo.domain.Laptop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LaptopRepository extends JpaRepository<Laptop, String> {
    List<Laptop> findByUser_UserId(String userId);
    Optional<Laptop> findTopByUser_EmailOrderByIssuedAtDesc(String email);
}
