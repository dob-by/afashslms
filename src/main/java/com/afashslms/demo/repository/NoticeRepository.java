package com.afashslms.demo.repository;

import com.afashslms.demo.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByCreatedAtDesc();
    Page<Notice> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}