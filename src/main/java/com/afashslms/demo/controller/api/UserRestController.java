package com.afashslms.demo.controller.api;

import com.afashslms.demo.domain.User;
import com.afashslms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    @GetMapping("/search")
    public List<UserSummary> searchUsers(@RequestParam("query") String query) {
        List<User> matchedUsers = userService.searchByUsernameOrUserId(query);
        return matchedUsers.stream()
                .map(user -> new UserSummary(user.getUserId(), user.getUsername()))
                .toList();
    }

    public record UserSummary(String userId, String username) {}
}
