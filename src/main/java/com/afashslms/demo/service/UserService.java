package com.afashslms.demo.service;


import com.afashslms.demo.domain.Role;
import com.afashslms.demo.domain.User;


import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    void changeRole(String userId, String newRole);
    Optional<User> findByEmail(String email);
    User findByUserId(String userId);
    List<User> searchByUsernameOrUserId(String keyword);
    List<User> searchUsers(String query);
    boolean updatePassword(String email, String currentPassword, String newPassword);
    long countPendingAdmins();
    List<User> findByRole(Role role);
    boolean approvePendingAdmin(String userId);
    void updateAdminProfile(String userId, String username, String militaryId, String affiliation, String unit);
    void save(User user);
    void registerPendingAdmin(String email, String provider, String username, String militaryId, String affiliation, String unit);
}

