package com.ravindra.kycdemo.contr;

import com.ravindra.kycdemo.service.AuthService;
import com.ravindra.kycdemo.model.User;
import com.ravindra.kycdemo.Repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            String token = authService.generateToken(username);

            User user = userRepository.findByUsername(username).orElse(null);
            boolean isPanVerified = user != null && Boolean.TRUE.equals(user.getIsPanVerified());
            boolean isVideoUploaded = user != null && user.getVideoPath() != null && !user.getVideoPath().isEmpty();
            String role = (user != null && user.getRole() != null) ? user.getRole() : "ROLE_USER";

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", role);
            response.put("isPanVerified", isPanVerified);
            response.put("isVideoUploaded", isVideoUploaded);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Not Authenticated"));
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        response.put("isPanVerified", Boolean.TRUE.equals(user.getIsPanVerified()));
        response.put("panNumber", user.getPanNumber());
        response.put("videoPath", user.getVideoPath());

        return ResponseEntity.ok(response);
    }
}
