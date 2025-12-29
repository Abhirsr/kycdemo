package com.ravindra.kycdemo.contr;

import com.ravindra.kycdemo.service.PanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/kyc")
public class PanController {

    private static final Logger logger = LoggerFactory.getLogger(PanController.class);

    @Autowired
    private PanService panService;

    @PostMapping("/verify-pan")
    public ResponseEntity<?> handlePanVerification(@RequestBody Map<String, String> request) {
        String panNumber = request.get("panNumber");
        String panName = request.get("panName");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        logger.info("Processing PAN verification for user: {}", username);

        Map<String, Object> result = panService.verifyPanDetails(panNumber, panName, username);
        String status = (String) result.get("status");

        if ("SUCCESS".equals(status)) {
            logger.info("PAN verification successful for user: {}", username);
            return ResponseEntity.ok(result);
        } else if ("LOCKED".equals(status)) {
            logger.warn("User {} is locked out.", username);
            return ResponseEntity.status(423).body(result);
        } else {
            logger.warn("PAN verification failed for user: {}", username);
            return ResponseEntity.badRequest().body(result);
        }
    }
}