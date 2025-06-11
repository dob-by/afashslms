package com.afashslms.demo.config;

import com.afashslms.demo.service.CustomOAuth2UserService;
import com.afashslms.demo.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
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

    // 로그인 성공 후 리디렉션 처리
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                System.out.println("Authenticated User: " + auth.getName());
                System.out.println("Granted Authorities: " + auth.getAuthorities());
            }
            new DefaultRedirectStrategy().sendRedirect(request, response, "/");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(authenticationProvider());
        AuthenticationManager authenticationManager = builder.getOrBuild(); // ✅ build() 대신 getOrBuild()

        CustomAuthenticationFilter customFilter = new CustomAuthenticationFilter(authenticationManager);
        customFilter.setFilterProcessesUrl("/login");
        customFilter.setUsernameParameter("username");
        customFilter.setPasswordParameter("password");

        customFilter.setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/login", "POST")
        );

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/import/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/users/check-email",
                                "/users/check-userid",
                                "/signup",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/h2-console/**",
                                "/import/**"
                        ).permitAll()
                        .requestMatchers("/admin/users/**", "/admin/laptops/**").hasAnyRole("MID_ADMIN", "TOP_ADMIN")

                        .requestMatchers("/mypage/password").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterAt(customFilter, UsernamePasswordAuthenticationFilter.class) // 🔥 여기서 커스텀 필터만 추가
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(successHandler())
                        .failureHandler((request, response, exception) -> {
                            String errorMessage = exception.getMessage();
                            if (errorMessage == null || errorMessage.isBlank()) {
                                errorMessage = "OAuth2 로그인 실패";
                            }

                            System.out.println("OAuth2 Login Failed: " + errorMessage);
                            errorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

                            response.sendRedirect("/login?errorMessage=" + errorMessage);
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .authenticationManager(authenticationManager); // ✅ 명시적으로 등록

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }

}
