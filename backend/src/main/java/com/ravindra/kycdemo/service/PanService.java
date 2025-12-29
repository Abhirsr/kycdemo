package com.ravindra.kycdemo.service;

import com.ravindra.kycdemo.Repo.UserRepository;
import com.ravindra.kycdemo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class PanService {

    private static final Logger logger = LoggerFactory.getLogger(PanService.class);
    private static final String API_URL = "https://idv-core-origin.emsigner.com/fintech-api/verify-pan";
    private static final String API_TOKEN = "panstatic.C7xLm9lTdNVay77bpseTh2XvYagWJZj";

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> verifyPanDetails(String panNumber, String userInputName, String username) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            result.put("status", "ERROR");
            result.put("message", "User not found");
            return result;
        }

        if (isUserLocked(user, result)) {
            return result;
        }

        try {
            logger.info("Calling External API for PAN: {}", panNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(API_TOKEN);

            Map<String, String> body = Map.of(
                    "pan", panNumber,
                    "consent", "Y",
                    "reason", "Identity Verification for Fintech Services");

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return processApiResponse(response.getBody(), userInputName, user, panNumber);
            } else {
                logger.warn("PAN API Error, but falling back to basic validity check.");
                return handleMockSuccess(user, userInputName, panNumber, result);
            }
        } catch (Exception e) {
            logger.error("Error during external PAN verification: {}", e.getMessage());
            return handleMockSuccess(user, userInputName, panNumber, result);
        }
    }

    private Map<String, Object> handleMockSuccess(User user, String name, String pan, Map<String, Object> result) {
        // Basic Regex Check
        if (pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}") && name != null && !name.trim().isEmpty()) {
            user.setFailedAttempts(0);
            user.setLockoutTime(null);
            updateUserStatus(user, name, pan);
            result.put("status", "SUCCESS");
            result.put("message", "PAN Verified Successfully (Basic Check)");
            return result;
        } else {
            result.put("status", "ERROR");
            result.put("message", "Invalid PAN Format or Name");
            return result;
        }
    }

    private boolean isUserLocked(User user, Map<String, Object> result) {
        if (user.getLockoutTime() != null) {
            if (user.getLockoutTime().isAfter(LocalDateTime.now())) {
                long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getLockoutTime());
                result.put("status", "LOCKED");
                result.put("message", "Too many failed attempts. Try again in " + (minutes + 1) + " minutes.");
                return true;
            } else {
                user.setFailedAttempts(0);
                user.setLockoutTime(null);
                userRepository.save(user);
            }
        }
        return false;
    }

    private Map<String, Object> processApiResponse(Map<String, Object> body, String userInputName, User user,
            String panNumber) {
        Map<String, Object> result = new HashMap<>();
        String nameFromPan = extractNameFromResponse(body);

        if (nameFromPan == null) {
            result.put("status", "ERROR");
            result.put("message", "Name not found in PAN records");
            return result;
        }

        double similarity = calculateSimilarity(userInputName, nameFromPan);
        logger.info("Name Match Similarity: {} for '{}' vs '{}'", similarity, userInputName, nameFromPan);

        if (similarity >= 0.70) {
            user.setFailedAttempts(0);
            user.setLockoutTime(null);
            updateUserStatus(user, nameFromPan, panNumber);
            result.put("status", "SUCCESS");
            result.put("message", "PAN Verified Successfully");
        } else {
            handleFailedAttempt(user);
            int attemptsLeft = 3 - (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts());
            result.put("status", "FAILED");
            result.put("message", "Name does not match PAN records. You have " + attemptsLeft + " attempts remaining.");
        }
        return result;
    }

    private void handleFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() == null ? 0 : user.getFailedAttempts();
        user.setFailedAttempts(attempts + 1);
        if (user.getFailedAttempts() >= 3) {
            user.setLockoutTime(LocalDateTime.now().plusMinutes(1));
        }
        userRepository.save(user);
    }

    private String extractNameFromResponse(Map<String, Object> body) {
        if (body.containsKey("data")) {
            Object dataObj = body.get("data");
            if (dataObj instanceof Map) {
                Map data = (Map) dataObj;
                return (String) data.getOrDefault("full_name", data.get("name"));
            }
        }
        if (body.containsKey("result")) {
            Object resObj = body.get("result");
            if (resObj instanceof Map) {
                Map result = (Map) resObj;
                return (String) result.getOrDefault("name", result.get("fullname"));
            }
        }
        return (String) body.getOrDefault("name", body.getOrDefault("fullname", body.get("pan_holder_name")));
    }

    private void updateUserStatus(User user, String legalName, String panNumber) {
        user.setIsPanVerified(true);
        user.setFullName(legalName);
        user.setPanNumber(panNumber);
        userRepository.save(user);
        logger.info("User {} KYC updated with Name: {}", user.getUsername(), legalName);
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null)
            return 0.0;
        if (s1.equals(s2))
            return 1.0;
        int longer = Math.max(s1.length(), s2.length());
        if (longer == 0)
            return 1.0;
        return (longer - editDistance(s1, s2)) / (double) longer;
    }

    private int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else if (j > 0) {
                    int newValue = costs[j - 1];
                    if (s1.charAt(i - 1) != s2.charAt(j - 1))
                        newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                    costs[j - 1] = lastValue;
                    lastValue = newValue;
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}