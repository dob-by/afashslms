package com.afashslms.demo.controller;

import com.afashslms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/check-userid")
    public ResponseEntity<Boolean> checkUserId(@RequestParam String userId) {
        boolean isAvailable = !userRepository.findByUserId(userId).isPresent(); // 사용 가능하면 true
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.findByEmail(email).isPresent();
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-militaryid")
    public boolean checkMilitaryId(@RequestParam String militaryId) {
        return !userRepository.findByMilitaryId(militaryId).isPresent(); // 사용 가능 여부 반환
    }
}