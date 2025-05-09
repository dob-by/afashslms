package com.afashslms.demo.service;


import com.afashslms.demo.domain.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    void changeRole(String userId, String newRole);

    User findByEmail(String email);
}