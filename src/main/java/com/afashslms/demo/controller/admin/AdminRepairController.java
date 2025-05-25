package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.RepairStatus;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.domain.Role;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminRepairController {

    private final RepairService repairService;

    @GetMapping("/admin/repairs")
    public String viewAllRepairs(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) RepairStatus status,
                                 Model model) {

        List<RepairRequest> repairs = repairService.searchRepairs(keyword, status);

        model.addAttribute("repairs", repairs);
        model.addAttribute("statuses", RepairStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin/repairs/list";
    }

    @GetMapping("/admin/repairs/{id}/edit")
    public String editRepairStatus(@PathVariable Long id, Model model) {
        RepairRequest repair = repairService.getRepairById(id);
        model.addAttribute("repair", repair);
        model.addAttribute("statuses", RepairStatus.values());
        return "admin/repairs/edit";
    }

    @PostMapping("/admin/repairs/{id}/status")
    public String updateRepairStatus(@PathVariable Long id,
                                     @RequestParam RepairStatus status,
                                     @RequestParam(required = false) String rejectionReason,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        RepairRequest repair = repairService.getRepairById(id);
        RepairStatus currentStatus = repair.getStatus();
        Role role = userDetails.getRole();

        if (role == Role.TOP_ADMIN) {
            // 총괄 관리자는 모든 상태로 변경 가능
            repair.setStatus(status);
            if (status == RepairStatus.REJECTED) {
                repair.setRejectionReason(rejectionReason); // 🧠 반려 사유 저장
            } else {
                repair.setRejectionReason(null); // ✔ 다른 상태면 반려 사유 초기화
            }
            repairService.saveRepairRequest(repair);

        } else if (role == Role.MID_ADMIN) {
            boolean valid =
                    (currentStatus == RepairStatus.REQUESTED && status == RepairStatus.IN_PROGRESS) ||
                            (currentStatus == RepairStatus.IN_PROGRESS && status == RepairStatus.COMPLETED);
            if (valid) {
                repair.setStatus(status);
                repairService.saveRepairRequest(repair);
            } else {
                throw new AccessDeniedException("중간관리자는 이 상태로 변경할 수 없습니다.");
            }

        } else {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        return "redirect:/admin/repairs/" + id;
    }

    @GetMapping("/admin/repairs/{id}")
    public String showRepairDetail(@PathVariable Long id, Model model) {
        RepairRequest repair = repairService.getRepairById(id);
        model.addAttribute("repair", repair);
        model.addAttribute("statuses", RepairStatus.values());
        return "admin/repairs/detail";
    }

    @PostMapping("/admin/repairs/{id}/reject")
    public String rejectRepair(@PathVariable Long id,
                               @RequestParam String rejectionReason,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Role role = userDetails.getRole();

        if (role != Role.MID_ADMIN && role != Role.TOP_ADMIN) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 중간관리자는 REQUESTED 상태일 때만 반려 가능
        RepairRequest repair = repairService.getRepairById(id);
        if (role == Role.MID_ADMIN && repair.getStatus() != RepairStatus.REQUESTED) {
            throw new AccessDeniedException("중간관리자는 해당 상태에서 반려할 수 없습니다.");
        }

        repairService.rejectRepair(id, rejectionReason);
        return "redirect:/admin/repairs/" + id;
    }
}