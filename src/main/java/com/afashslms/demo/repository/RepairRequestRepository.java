package com.afashslms.demo.repository;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.RepairStatus;
import com.afashslms.demo.domain.User;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    List<RepairRequest> findByUserOrderByCreatedAtDesc(User user);
    List<RepairRequest> findAllByUser_EmailOrderByCreatedAtDesc(String email);
    List<RepairRequest> findAllByOrderByCreatedAtDesc();

    long countByStatus(RepairStatus status);

    long countByStatusNot(RepairStatus status);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    // 이름 또는 이메일 검색
    @Query("SELECT r FROM RepairRequest r WHERE " +
            "LOWER(r.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY r.createdAt DESC")
    List<RepairRequest> findByKeyword(@Param("keyword") String keyword);

    // 이름/이메일 + 상태 검색
    @Query("SELECT r FROM RepairRequest r WHERE " +
            "(LOWER(r.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND r.status = :status ORDER BY r.createdAt DESC")
    List<RepairRequest> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") RepairStatus status);

    // 상태만으로 검색
    List<RepairRequest> findByStatusOrderByCreatedAtDesc(RepairStatus status);
}