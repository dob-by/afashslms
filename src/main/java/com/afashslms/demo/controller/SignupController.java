package com.afashslms.demo.controller;

import com.afashslms.demo.dto.SignupFormDto;
import com.afashslms.demo.service.SignupService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("signupForm", new SignupFormDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute("signupForm") SignupFormDto form, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "signup"; // 유효성 에러 처리
        }

        try {
            signupService.registerUser(form);
            return "redirect:/login?signupSuccess"; //회원가입 성공 시 로그인 페이지로
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }
}
