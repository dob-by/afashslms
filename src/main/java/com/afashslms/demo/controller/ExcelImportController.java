package com.afashslms.demo.controller;

import com.afashslms.demo.service.ExcelReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class ExcelImportController {

    private final ExcelReaderService excelReaderService;

    @PostMapping("/users")
    public ResponseEntity<String> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            excelReaderService.importUsersFromExcel(file);
            return ResponseEntity.ok("✅ 사용자 데이터 업로드 성공!");
        } catch (Exception e) {
            e.printStackTrace();  // 나중에 로그로 바꾸는 게 좋음
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ 사용자 데이터 업로드 실패: " + e.getMessage());
        }
    }

    @PostMapping("/laptops")
    public ResponseEntity<String> importLaptops(@RequestParam("file") MultipartFile file) {
        try {
            excelReaderService.importLaptopsFromExcel(file);
            return ResponseEntity.ok("✅ 노트북 데이터 업로드 성공!");
        } catch (Exception e) {
            e.printStackTrace();  // 나중엔 로깅으로!
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ 노트북 데이터 업로드 실패: " + e.getMessage());
        }
    }
}