//package com.afashslms.demo.security;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
//
//    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
//        super.setAuthenticationManager(authenticationManager);
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
//            throws AuthenticationException {
//
//        String loginType = request.getParameter("loginType"); // "student" 또는 "staff"
//        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//
//        if ("student".equals(loginType)) {
//            // TODO: 필요시 username → email로 매핑 (예: DB 조회 또는 캐시)
//            // username = studentIdToEmail(username);
//        }
//
//        UsernamePasswordAuthenticationToken authRequest =
//                new UsernamePasswordAuthenticationToken(username, password);
//
//        setDetails(request, authRequest);
//        return this.getAuthenticationManager().authenticate(authRequest);
//    }
//}
package com.afashslms.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import com.afashslms.demo.domain.Role;
import java.io.IOException;

@RequiredArgsConstructor
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        System.out.println("🔥 CustomAuthenticationFilter 작동!");

        String loginType = request.getParameter("loginType"); // "student" or "staff"
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        System.out.println(">> loginType: " + loginType);
        System.out.println(">> username: " + username);

        if ("student".equalsIgnoreCase(loginType)) {
            // TODO: username = studentIdToEmail(username);
            System.out.println(">> student login 시 이메일 변환 안됨 (임시)");
        }

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);

        setDetails(request, authRequest);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        System.out.println(">> 인증 완료됨");

        // PENDING_ADMIN 차단
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            Role role = customUserDetails.getUser().getRole();
            if (role == Role.PENDING_ADMIN) {
                System.out.println("⛔ 승인되지 않은 관리자 로그인 시도!");
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
            System.out.println(">> 학생 권한 없음");
            throw new BadCredentialsException("학생 로그인에는 학생 계정만 허용됩니다.");
        }

        if (isStaffLogin && !hasAdminRole) {
            System.out.println(">> 관리자 권한 없음");
            throw new BadCredentialsException("관리자 로그인에는 관리자 계정만 허용됩니다.");
        }

        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        System.out.println("✅ 로그인 성공 → 홈으로 리다이렉트");

        // 1. 인증 정보 SecurityContext에 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        // 2. 세션에 저장해주는 부분 (이게 없으면 다음 요청에서 인증 안 됨!)
        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);

        // 3. 홈으로 리다이렉트
        response.sendRedirect("/");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        System.out.println("❌ 로그인 실패: " + failed.getMessage());

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