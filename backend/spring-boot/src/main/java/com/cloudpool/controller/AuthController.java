package com.cloudpool.controller;

import com.cloudpool.model.Bucket;
import com.cloudpool.model.User;
import com.cloudpool.repository.BucketRepository;
import com.cloudpool.repository.UserRepository;
import com.cloudpool.security.JwtUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final BucketRepository bucketRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already in use"));
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // Auto-create default storage pool (bucket)
        Bucket defaultBucket = Bucket.builder()
                .user(savedUser)
                .name("default-pool")
                .description("Default storage pool created automatically")
                .isPublic(false)
                .build();
        bucketRepository.save(defaultBucket);

        String token = jwtUtils.generateToken(savedUser.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(403).body(Map.of("error", "User account is suspended"));
        }

        String token = jwtUtils.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("name", user.getName());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String name;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }
}
