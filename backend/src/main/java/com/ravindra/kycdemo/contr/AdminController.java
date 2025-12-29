package com.ravindra.kycdemo.contr;

import com.ravindra.kycdemo.Repo.UserRepository;
import com.ravindra.kycdemo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setRole(user.getRole() != null ? user.getRole() : "ROLE_USER");
        user.setIsPanVerified(user.getIsPanVerified() != null && user.getIsPanVerified());
        user.setFailedAttempts(user.getFailedAttempts() != null ? user.getFailedAttempts() : 0);

        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/users/bulk")
    public ResponseEntity<?> bulkAddUsers(@RequestBody List<User> users) {
        for (User user : users) {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                continue;
            }
            if (user.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            user.setRole(user.getRole() != null ? user.getRole() : "ROLE_USER");
            user.setIsPanVerified(user.getIsPanVerified() != null && user.getIsPanVerified());
            user.setFailedAttempts(user.getFailedAttempts() != null ? user.getFailedAttempts() : 0);

            userRepository.save(user);
        }
        return ResponseEntity.ok(Map.of("message", "Users processed successfully"));
    }

    @PostMapping("/users/{id}/reset")
    public ResponseEntity<?> resetUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsPanVerified(false);
            user.setVideoPath(null);
            userRepository.save(user);
        });
        return ResponseEntity.ok(Map.of("message", "User KYC Reset Successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User Deleted Successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        long total = userRepository.count();
        long verified = userRepository.countByIsPanVerified(true);
        long pending = total - verified; // or countByIsPanVerified(false)

        return ResponseEntity.ok(Map.of(
                "total", total,
                "verified", verified,
                "pending", pending));
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportUsers() {
        List<User> users = userRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Username,FullName,PAN,Verified,Video\n");

        for (User u : users) {
            csv.append(u.getId()).append(",");
            csv.append(escapeCsv(u.getUsername())).append(",");
            csv.append(escapeCsv(u.getFullName())).append(",");
            csv.append(escapeCsv(u.getPanNumber())).append(",");
            csv.append(u.getIsPanVerified() != null && u.getIsPanVerified() ? "Yes" : "No").append(",");
            csv.append(u.getVideoPath() != null ? "Yes" : "No").append("\n");
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv.toString());
    }

    private String escapeCsv(String data) {
        if (data == null)
            return "";
        return "\"" + data.replace("\"", "\"\"") + "\"";
    }
}
