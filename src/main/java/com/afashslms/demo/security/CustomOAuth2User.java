package com.afashslms.demo.security;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.*;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final User user;
    private final boolean isFirstLogin;
    private final String email;
    private final String provider;

    public CustomOAuth2User(User user, Map<String, Object> attributes, String email, String provider, boolean isFirstLogin) {
        super(
                Collections.singleton(() -> {
                    if (user != null && user.getRole() != null) {
                        return "ROLE_" + user.getRole().name();  // 실제 Role 기반으로 권한 부여
                    }
                    return "ROLE_TEMP";  // 기본값
                }),
                ensureEmailInAttributes(attributes, email),
                "email"
        );
        this.user = user;
        this.email = email;
        this.provider = provider;
        this.isFirstLogin = isFirstLogin;
    }

    // 실제 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user != null && user.getRole() != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_TEMP"));
    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public Role getRole() {
        if (user != null && user.getRole() != null) {
            return user.getRole();
        }
        return Role.TEMP;
    }

    private static Map<String, Object> ensureEmailInAttributes(Map<String, Object> attributes, String email) {
        Map<String, Object> modifiable = new HashMap<>(attributes);
        modifiable.putIfAbsent("email", email);
        return modifiable;
    }
}