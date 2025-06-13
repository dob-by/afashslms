package com.afashslms.demo.security;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            String email = customUserDetails.getUsername(); // username = email
            User user = userService.findByEmail(email).orElseThrow();

            if (user.getRole() == Role.STUDENT) {
                if (!user.isPasswordChanged()) {
                    response.sendRedirect("/mypage"); // 초기 비번인 학생은 마이페이지로
                } else {
                    response.sendRedirect("/"); // 비번 바꾼 학생은 홈으로!
                }
                return;
            }

            if (user.getRole() == Role.MID_ADMIN || user.getRole() == Role.TOP_ADMIN) {
                response.sendRedirect("/admin/mypage");
                return;
            }
        }

        // 기타 로그인은 홈으로
        response.sendRedirect("/");
    }
}