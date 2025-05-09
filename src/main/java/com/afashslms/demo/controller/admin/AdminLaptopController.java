package com.afashslms.demo.controller.admin;

import com.afashslms.demo.dto.LaptopViewDto;
import com.afashslms.demo.service.LaptopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminLaptopController {

    private final LaptopService laptopService;

    @GetMapping("/admin/laptops")
    public String showLaptopList(Model model) {
        List<LaptopViewDto> laptops = laptopService.getAllLaptopsForAdmin();
        model.addAttribute("laptops", laptops);
        return "admin/laptop-list";
    }
}