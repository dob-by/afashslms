package com.afashslms.demo.service;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.dto.SignupForm;
import com.afashslms.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class SignupServiceTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입이 정상적으로 수행된다")
    void registerUser_success() {
        // given
        SignupForm form = new SignupForm();
        form.setUserId("testuser");
        form.setUsername("테스트유저");
        form.setEmail("test@example.com");
        form.setPassword("password123");
        form.setMilitaryId("G123456");
        form.setRole("STUDENT");

        // when
        signupService.registerUser(form);

        // then
        User savedUser = userRepository.findByUserId("testuser").orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("테스트유저");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("중복 아이디로 회원가입 시 예외 발생")
    void registerUser_duplicateUserId() {
        // given
        SignupForm form1 = new SignupForm();
        form1.setUserId("dupuser");
        form1.setUsername("유저1");
        form1.setEmail("email1@test.com");
        form1.setPassword("pass1");
        form1.setMilitaryId("G111111");
        form1.setRole("STUDENT");

        SignupForm form2 = new SignupForm();
        form2.setUserId("dupuser"); // 같은 아이디
        form2.setUsername("유저2");
        form2.setEmail("email2@test.com");
        form2.setPassword("pass2");
        form2.setMilitaryId("G222222");
        form2.setRole("STUDENT");

        // when
        signupService.registerUser(form1);
        Throwable thrown = catchThrowable(() -> signupService.registerUser(form2));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 아이디입니다.");
    }


    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외 발생")
    void registerUser_duplicateEmail() {
        // given
        SignupForm form1 = new SignupForm();
        form1.setUserId("user1");
        form1.setUsername("유저1");
        form1.setEmail("same@test.com");
        form1.setPassword("pass1");
        form1.setMilitaryId("G111111");
        form1.setRole("STUDENT");

        SignupForm form2 = new SignupForm();
        form2.setUserId("user2");
        form2.setUsername("유저2");
        form2.setEmail("same@test.com"); // 같은 이메일
        form2.setPassword("pass2");
        form2.setMilitaryId("G222222");
        form2.setRole("STUDENT");

        // when
        signupService.registerUser(form1);
        Throwable thrown = catchThrowable(() -> signupService.registerUser(form2));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다.");
    }
}