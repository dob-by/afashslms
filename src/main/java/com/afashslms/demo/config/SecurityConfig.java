package com.afashslms.demo.config;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.security.CustomAuthenticationSuccessHandler;
import com.afashslms.demo.security.CustomOAuth2User;
import com.afashslms.demo.security.CustomUserDetails;
import com.afashslms.demo.service.CustomOAuth2UserService;
import com.afashslms.demo.service.CustomUserDetailsService;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.afashslms.demo.security.CustomAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserService userService;

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();

                // OAuth2 로그인 사용자
                if (principal instanceof CustomOAuth2User customOAuthUser) {
                    User user = customOAuthUser.getUser();

                    // user가 null인 경우 → 최초 로그인
                    if (user == null) {
                        request.getSession().setAttribute("oauthEmail", customOAuthUser.getEmail());
                        request.getSession().setAttribute("oauthProvider", customOAuthUser.getProvider());
                        redirectStrategy.sendRedirect(request, response, "/admin/profile");
                        return;
                    }

                    // Role == TEMP → 최초 로그인 처리
                    if (user.getRole() == Role.TEMP) {
                        request.getSession().setAttribute("oauthEmail", customOAuthUser.getEmail());
                        request.getSession().setAttribute("oauthProvider", customOAuthUser.getProvider());
                        redirectStrategy.sendRedirect(request, response, "/admin/profile");
                        return;
                    }

                    // 프로필 미완성
                    if (!user.isProfileComplete()) {
                        redirectStrategy.sendRedirect(request, response, "/admin/profile");
                        return;
                    }

                    // 모든 관리자는 로그인 후 홈으로 이동
                    redirectStrategy.sendRedirect(request, response, "/");
                    return;
                }

                // 일반 관리자 (로컬 로그인)
                if (principal instanceof CustomUserDetails customUser) {
                    User user = customUser.getUser();
                    if (user.getRole().name().contains("ADMIN")) {
                        redirectStrategy.sendRedirect(request, response, "/admin/mypage");
                        return;
                    }
                    // 학생
                    if (user.getRole() == Role.STUDENT) {
                        if (!user.isPasswordChanged()) {
                            System.out.println("[DEBUG] 비밀번호 변경 안한 학생 → /mypage");
                            redirectStrategy.sendRedirect(request, response, "/mypage");
                        } else {
                            System.out.println("[DEBUG] 비밀번호 변경한 학생 → /");
                            redirectStrategy.sendRedirect(request, response, "/");
                        }
                    }
                }
            }
        };
    }

    // Security 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(authenticationProvider());
        AuthenticationManager authenticationManager = builder.getOrBuild();

        CustomAuthenticationFilter customFilter =
                new CustomAuthenticationFilter(authenticationManager, successHandler());

        customFilter.setFilterProcessesUrl("/login");
        customFilter.setUsernameParameter("username");
        customFilter.setPasswordParameter("password");
        customFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
        customFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            String errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";

            if (exception.getMessage().contains("관리자 승인이 필요합니다")) {
                errorMessage = exception.getMessage();
            } else if (exception.getMessage().contains("관리자 로그인에는")) {
                errorMessage = exception.getMessage();
            } else if (exception.getMessage().contains("학생 로그인에는")) {
                errorMessage = exception.getMessage();
            }

            response.sendRedirect("/login?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
        });
        customFilter.setAuthenticationSuccessHandler(successHandler());

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/import/**", "/qna/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/users/check-email", "/users/check-userid", "/signup", "/login",
                                "/css/**", "/js/**", "/h2-console/**", "/import/**"
                        ).permitAll()

                        // 공개 Q&A 페이지 허용
                        .requestMatchers("/qna/**").permitAll()

                        // TEMP의 GET/POST 모두 허용하도록 분리
                        .requestMatchers(HttpMethod.GET, "/admin/profile").hasAnyRole("TEMP", "MID_ADMIN", "TOP_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/admin/profile").hasRole("TEMP")

                        // 관리자 권한자도 프로필 페이지 접근 허용
                        .requestMatchers("/admin/profile").hasAnyRole("TEMP", "MID_ADMIN", "TOP_ADMIN")

                        // TEMP는 나머지 전부 접근 차단
                        .requestMatchers("/**").not().hasRole("TEMP")

                        // 관리자 페이지
                        .requestMatchers("/admin/pending-admins").hasRole("TOP_ADMIN")
                        .requestMatchers("/admin/users/**", "/admin/laptops/**", "/admin/mypage")
                        .hasAnyRole("MID_ADMIN", "TOP_ADMIN")

                        .requestMatchers("/mypage").hasAnyRole("STUDENT", "MID_ADMIN", "TOP_ADMIN")

                        // 일반 사용자용 경로
                        .requestMatchers("/mypage/password").authenticated()

                        // 그 외 요청 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterAt(customFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(successHandler())
                        .failureHandler((request, response, exception) -> {
                            String errorMessage = "OAuth2 로그인 실패"; // 기본값

                            if (exception instanceof OAuth2AuthenticationException oAuth2Ex) {
                                String errorCode = oAuth2Ex.getError().getErrorCode();

                                if ("pending_admin".equals(errorCode)) {
                                    errorMessage = "승인 대기 중인 관리자입니다. 관리자 승인이 필요합니다.";
                                } else if (oAuth2Ex.getError().getDescription() != null && !oAuth2Ex.getError().getDescription().isBlank()) {
                                    errorMessage = oAuth2Ex.getError().getDescription();
                                }
                            } else {
                                String msg = exception.getMessage();
                                if (msg != null && !msg.isBlank()) {
                                    errorMessage = msg;
                                }
                            }

                            System.out.println("OAuth2 Login Failed: " + errorMessage);
                            response.sendRedirect("/login?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
                        })
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .authenticationManager(authenticationManager);

        return http.build();
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(HttpSecurity http) throws Exception {
        return new CustomAuthenticationFilter(
                authenticationManager(http),                        // 인증 매니저 넘기기
                new CustomAuthenticationSuccessHandler(userService) // 커스텀 성공 핸들러 넘기기
        );
    }

    // 인증 공급자 설정
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof CustomOAuth2User customUser) {
                    if (customUser.getRole() == Role.TEMP) {
                        response.sendRedirect("/admin/profile?error=need_profile");
                        return;
                    }
                }
            }

            response.sendRedirect("/error/403");
        };
    }
}