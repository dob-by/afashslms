package com.afashslms.demo.service;


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

}

