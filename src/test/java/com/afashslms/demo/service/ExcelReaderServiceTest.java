package com.afashslms.demo.service;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ExcelReaderServiceTest {

    @Autowired
    private ExcelReaderService excelReaderService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void test_중복군번_검증() {
        // 1. 이미 있는 데이터 저장
        User user = new User();
        user.setUserId("A1234");
        user.setEmail("test@example.com");
        userRepository.save(user);

        // 2. 같은 군번 → 중복 예외 발생 기대
        assertThrows(RuntimeException.class, () -> {
            excelReaderService.validateUserDuplicate("A1234", "newemail@example.com");
        });

        // 3. 같은 이메일 → 중복 예외 발생 기대
        assertThrows(RuntimeException.class, () -> {
            excelReaderService.validateUserDuplicate("NEWID", "test@example.com");
        });

        // 4. 중복 없으면 통과
        excelReaderService.validateUserDuplicate("NEWID", "newemail@example.com");
    }
}