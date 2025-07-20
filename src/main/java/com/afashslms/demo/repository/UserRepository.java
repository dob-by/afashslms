package com.afashslms.demo.repository;

import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);
    Optional<User> findByMilitaryId(String militaryId);
    List<User> findByUsernameContainingIgnoreCaseOrUserIdContainingIgnoreCase(String username, String userId);
    List<User> findByUserIdContainingIgnoreCaseOrUsernameContainingIgnoreCase(String userIdPart, String usernamePart);
    long countByRole(Role role);
    List<User> findByRole(Role role);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
}
