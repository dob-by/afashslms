package com.afashslms.demo.controller;

import com.afashslms.demo.dto.SignupForm;
import com.afashslms.demo.service.SignupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute("signupForm") SignupForm form, Model model) {
        try {
            signupService.registerUser(form);
            return "login"; //회원가입 성공 시 로그인 페이지로
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }
}
