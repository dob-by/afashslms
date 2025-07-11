package com.afashslms.demo.service;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.RepairStatus;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.RepairRequestRepository;
import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRequestRepository repairRequestRepository;
    private final UserRepository userRepository;
    private final OwnershipService ownershipService;

    public List<RepairRequest> getRepairsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        return repairRequestRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void saveRepairRequest(RepairRequest request) {
        repairRequestRepository.save(request);
    }

    public RepairRequest getRepairById(Long id) {
        return repairRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수리 요청을 찾을 수 없습니다."));
    }

    public void rejectRepair(Long id, String reason) {
        RepairRequest repair = getRepairById(id);
        repair.setStatus(RepairStatus.REJECTED);
        repair.setRejectionReason(reason);
        repairRequestRepository.save(repair);
    }

    public void updateRepair(Long id, RepairRequest updatedRequest, String email) {
        RepairRequest existing = getRepairById(id);
        if (!existing.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("본인의 요청만 수정할 수 있습니다.");
        }

        existing.setCategory(updatedRequest.getCategory());
        existing.setDetailType(updatedRequest.getDetailType());
        existing.setDescription(updatedRequest.getDescription());
        existing.setManager(updatedRequest.getManager());
        existing.setCmosPassword(updatedRequest.getCmosPassword());
        existing.setWindowsPassword(updatedRequest.getWindowsPassword());

        repairRequestRepository.save(existing);
    }

    public void deleteRepair(Long id, String email) {
        RepairRequest existing = getRepairById(id);
        if (!existing.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("본인의 요청만 삭제할 수 있습니다.");
        }

        repairRequestRepository.deleteById(id);
    }

    public List<RepairRequest> findAllByStudentEmail(String email) {
        return repairRequestRepository.findAllByUser_EmailOrderByCreatedAtDesc(email);
    }

    public List<RepairRequest> findAll() {
        return repairRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public void updateRepairStatus(Long id, RepairStatus status) {
        RepairRequest repair = getRepairById(id);
        repair.setStatus(status);
        repairRequestRepository.save(repair);
    }

    public List<RepairRequest> searchRepairs(String keyword, RepairStatus status) {
        if ((keyword == null || keyword.isBlank()) && status == null) {
            return repairRequestRepository.findAllByOrderByCreatedAtDesc();
        }

        if (status != null && (keyword == null || keyword.isBlank())) {
            return repairRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        }

        if (status == null) {
            return repairRequestRepository.findByKeyword(keyword);
        }

        return repairRequestRepository.findByKeywordAndStatus(keyword, status);
    }

    //수리 관련 통계 확인 메서드
    public long countAll() {
        return repairRequestRepository.count();
    }

    public long countByStatus(RepairStatus status) {
        return repairRequestRepository.countByStatus(status);
    }

    public long countByStatusNot(RepairStatus status) {
        return repairRequestRepository.countByStatusNot(status);
    }

    public long countThisWeek() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        return repairRequestRepository.countByCreatedAtAfter(oneWeekAgo);
    }
}