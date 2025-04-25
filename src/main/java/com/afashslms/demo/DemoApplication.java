package com.afashslms.demo;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import com.afashslms.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {

		SpringApplication.run(DemoApplication.class, args);
	}
	// 여기에 직접 CommandLineRunner 임시 등록
	@Bean
	public CommandLineRunner testUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			userRepository.deleteAll(); // ✅ DB 리셋용 (임시!)
			System.out.println("🔥 main에서 실행됨!!!");

			boolean exists = userRepository.findByUserId("admin").isPresent();
			System.out.println("🔍 admin 유저 존재 여부: " + exists);

			if (!exists) {
				System.out.println("🛠 admin 유저 생성 시작!");
				User user = new User();
				user.setUserId("admin");
				user.setPassword(passwordEncoder.encode("1234"));
				user.setUsername("관리자");
				user.setEmail("admin@test.com");
				user.setProvider("local");
				user.setRole(Role.TOP_ADMIN);
				user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
				userRepository.save(user);
				System.out.println("✅ 테스트 유저 생성 완료 (main에서)");
			} else {
				System.out.println("⛔ 이미 admin 유저가 DB에 있음");
			}
		};
	}
}