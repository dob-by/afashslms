package com.afashslms.demo.service;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.domain.Laptop;
import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.LaptopStatus;
import com.afashslms.demo.repository.UserRepository;
import com.afashslms.demo.repository.LaptopRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelReaderService {

    private final UserRepository userRepository;
    private final LaptopRepository laptopRepository;

    public ExcelReaderService(UserRepository userRepository, LaptopRepository laptopRepository) {
        this.userRepository = userRepository;
        this.laptopRepository = laptopRepository;
    }

    public void importUsersFromExcel(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        List<User> users = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // skip header

            String userId = getCellValue(row.getCell(0));
            String username = getCellValue(row.getCell(1));
            String email = getCellValue(row.getCell(2));
            String militaryId = getCellValue(row.getCell(3));
            LocalDateTime createdAt = LocalDateTime.parse(getCellValue(row.getCell(5)));

            User user = new User();
            user.setUserId(userId);
            user.setUsername(username);
            user.setEmail(email);
            user.setMilitaryId(militaryId);
            user.setRole(Role.STUDENT);
            user.setCreatedAt(Timestamp.valueOf(createdAt));
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

            String deviceId = getCellValue(row.getCell(0));
            String ip = getCellValue(row.getCell(1));
            String modelName = getCellValue(row.getCell(2));
            String statusStr = getCellValue(row.getCell(3));
            int manageNumber = Integer.parseInt(getCellValue(row.getCell(4)));
            String studentEmail = getCellValue(row.getCell(5));
            LocalDateTime createdAt = LocalDateTime.parse(getCellValue(row.getCell(6)));

            LaptopStatus status = mapToLaptopStatus(statusStr);

            Laptop laptop = new Laptop();
            laptop.setDeviceId(deviceId);
            laptop.setIp(ip);
            laptop.setModelName(modelName);
            laptop.setStatus(status);
            laptop.setManageNumber(manageNumber);
            laptop.setManageNumber(manageNumber);

            userRepository.findByEmail(studentEmail).ifPresent(laptop::setUser);

            laptops.add(laptop);
        }

        laptopRepository.saveAll(laptops);
        workbook.close();
    }

    private String getCellValue(Cell cell) {
        return (cell == null) ? "" : cell.toString().trim();
    }

    private LaptopStatus mapToLaptopStatus(String statusStr) {
        return switch (statusStr) {
            case "사용 중" -> LaptopStatus.IN_USE;
            case "수리 중" -> LaptopStatus.IN_REPAIR;
            case "수리 요청됨" -> LaptopStatus.REPAIR_REQUESTED;
            case "대기 중" -> LaptopStatus.AVAILABLE;
            default -> throw new IllegalArgumentException("Unknown status: " + statusStr);
        };
    }
}