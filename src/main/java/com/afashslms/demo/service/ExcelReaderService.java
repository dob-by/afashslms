package com.afashslms.demo.service;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.LaptopStatus;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.repository.LaptopRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelReaderService {

    private final UserRepository userRepository;
    private final LaptopRepository laptopRepository;
    private final PasswordEncoder passwordEncoder;

    public ExcelReaderService(UserRepository userRepository, LaptopRepository laptopRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.laptopRepository = laptopRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void importUsersFromExcel(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        List<User> users = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // skip header

            String username = getCellValue(row.getCell(1));
            String email = getCellValue(row.getCell(2));
            String militaryId = getCellValue(row.getCell(3));
            String createdAtStr = getCellValue(row.getCell(5));
            String birth = getCellValue(row.getCell(6));  // 생년월일 (형식: 090305)

            String userId = militaryId; // ✅ 핵심: 군번을 userId로

            LocalDateTime createdAt = parseFlexibleDate(createdAtStr);
            String rawPassword = militaryId + birth;

            User user = new User();
            user.setUserId(userId); // 이제 이건 militaryId 기반
            user.setUsername(username);
            user.setEmail(email);
            user.setMilitaryId(militaryId);
            user.setBirth(birth);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(Role.STUDENT);
            user.setCreatedAt(createdAt != null ? Timestamp.valueOf(createdAt) : new Timestamp(System.currentTimeMillis()));

            users.add(user);
        }

        userRepository.saveAll(users);
        workbook.close();
    }

    public void importLaptopsFromExcel(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        List<Laptop> laptops = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String deviceId = getCellValue(row.getCell(0));         // A열: 일련번호
            String modelName = getCellValue(row.getCell(1));        // B열: 모델명
            String ip = getCellValue(row.getCell(2));               // C열: IP
            String statusStr = getCellValue(row.getCell(3));        // D열: 상태
            // E열 생략 (current_status)
            int manageNumber = Integer.parseInt(getCellValue(row.getCell(5))); // F열
            String studentUserId = getCellValue(row.getCell(6));    // G열: 사용자 ID (이메일 아님)
            String createdAtStr = getCellValue(row.getCell(7));     // H열: 발급일자


            LocalDateTime createdAt = parseFlexibleDate(createdAtStr);
            LaptopStatus status = mapToLaptopStatus(statusStr);

            Laptop laptop = new Laptop();
            laptop.setDeviceId(deviceId);
            laptop.setModelName(modelName);
            laptop.setIp(ip);
            laptop.setStatus(status);
            laptop.setManageNumber(manageNumber);
            laptop.setIssuedAt(createdAt);

            // ✅ user_id 기준으로 유저 찾기
            userRepository.findByUserId(studentUserId).ifPresent(laptop::setUser);

            laptops.add(laptop);
        }

        laptopRepository.saveAll(laptops);
        workbook.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDateTime ldt = cell.getLocalDateTimeCellValue();
                    yield ldt.toString(); // ISO format
                } else {
                    double num = cell.getNumericCellValue();
                    yield (num == (int) num) ? String.valueOf((int) num) : String.valueOf(num);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private LocalDateTime parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        try {
            return LocalDateTime.parse(dateStr); // ISO (e.g. 2024-03-02T00:00)
        } catch (DateTimeParseException e1) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(dateStr, formatter); // e.g. 2024-03-02 00:00:00
            } catch (DateTimeParseException e2) {
                throw new RuntimeException("❌ 날짜 형식을 파싱할 수 없습니다: " + dateStr);
            }
        }
    }

    private LaptopStatus mapToLaptopStatus(String statusStr) {
        return switch (statusStr) {
            case "사용 중" -> LaptopStatus.IN_USE;
            case "수리 중" -> LaptopStatus.IN_REPAIR;
            case "수리 요청됨" -> LaptopStatus.REPAIR_REQUESTED;
            case "대기 중" -> LaptopStatus.AVAILABLE;
            default -> throw new IllegalArgumentException("❌ 잘못된 상태 값: " + statusStr);
        };
    }
}