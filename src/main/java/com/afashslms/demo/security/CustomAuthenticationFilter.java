package com.afashslms.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import com.afashslms.demo.domain.Role;
import java.io.IOException;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationSuccessHandler successHandler;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      AuthenticationSuccessHandler successHandler) {
        this.authenticationManager = authenticationManager;
        this.successHandler = successHandler;
        super.setAuthenticationManager(authenticationManager);
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String loginType = request.getParameter("loginType"); // "student" or "staff"
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if ("student".equalsIgnoreCase(loginType)) {
            // TODO: username = studentIdToEmail(username);
        }

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);

        setDetails(request, authRequest);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        // PENDING_ADMIN 차단
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            Role role = customUserDetails.getUser().getRole();
            if (role == Role.PENDING_ADMIN) {
                throw new BadCredentialsException("관리자 승인이 필요합니다.");
            }
        }

        boolean isStudentLogin = "student".equalsIgnoreCase(loginType);
        boolean isStaffLogin = "staff".equalsIgnoreCase(loginType);

        boolean hasStudentRole = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith("ROLE_MID_ADMIN") || a.getAuthority().startsWith("ROLE_TOP_ADMIN"));

        if (isStudentLogin && !hasStudentRole) {
            throw new BadCredentialsException("학생 로그인에는 학생 계정만 허용됩니다.");
        }

        if (isStaffLogin && !hasAdminRole) {
            throw new BadCredentialsException("관리자 로그인에는 관리자 계정만 허용됩니다.");
        }

        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);

        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {

        String errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        String exceptionMessage = failed.getMessage();

        if (exceptionMessage.contains("관리자 승인이 필요")) {
            errorMessage = "승인 대기 중인 관리자입니다. 관리자 승인이 필요합니다.";
        } else if (exceptionMessage.contains("학생 로그인에는")) {
            errorMessage = "학생 로그인에는 학생 계정만 허용됩니다.";
        } else if (exceptionMessage.contains("관리자 로그인에는")) {
            errorMessage = "관리자 로그인에는 관리자 계정만 허용됩니다.";
        }

        response.sendRedirect("/login?errorMessage=" + java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8));
    }
}