package com.afashslms.demo.controller.admin;

import com.afashslms.demo.domain.RepairRequest;
import com.afashslms.demo.domain.RepairStatus;
import com.afashslms.demo.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminRepairController {

    private final RepairService repairService;

    @GetMapping("/admin/repairs")
    public String viewAllRepairs(Model model) {
        List<RepairRequest> allRepairs = repairService.findAll();
        model.addAttribute("repairs", allRepairs);
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
    public String updateRepairStatus(@PathVariable Long id, @RequestParam RepairStatus status) {
        repairService.updateRepairStatus(id, status);
        return "redirect:/admin/repairs";
    }

    @GetMapping("/admin/repairs/{id}")
    public String showRepairDetail(@PathVariable Long id, Model model) {
        RepairRequest repair = repairService.getRepairById(id);
        model.addAttribute("repair", repair);
        model.addAttribute("statuses", RepairStatus.values());
        return "admin/repairs/detail";
    }
}