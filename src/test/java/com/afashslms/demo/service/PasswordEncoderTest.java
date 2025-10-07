package com.afashslms.demo.service;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    @Test
    void generate() {
        String hash = new BCryptPasswordEncoder().encode("1234");
        System.out.println("New Hash: " + hash);
    }
}