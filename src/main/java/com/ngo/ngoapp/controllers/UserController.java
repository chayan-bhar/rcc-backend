package com.ngo.ngoapp.controllers;

import com.ngo.ngoapp.auth.UserPrincipal;
import com.ngo.ngoapp.models.User;
import com.ngo.ngoapp.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return userRepository.findById(principal.getUid())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody User profileUpdate) {
        if (principal == null) return ResponseEntity.status(401).build();
        return userRepository.findById(principal.getUid())
                .map(user -> {
                    if (profileUpdate.getName() != null) {
                        user.setName(profileUpdate.getName());
                    }
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
