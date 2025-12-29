package com.ravindra.kycdemo.service;

import com.ravindra.kycdemo.Repo.UserRepository;
import com.ravindra.kycdemo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class VideoService {

    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> processVideoUpload(MultipartFile file, String username) throws IOException {
        Path uploadDir = Paths.get("uploads").toAbsolutePath();
        Path filePath = uploadDir.resolve("KYC_" + username + ".webm");

        if (!Files.exists(uploadDir)) {
            logger.info("Creating upload directory: {}", uploadDir);
            Files.createDirectories(uploadDir);
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Video saved to: {}", filePath);

        String videoUrl = "/uploads/KYC_" + username + ".webm";

        User user = userRepository.findByUsername(username).orElseThrow();
        user.setVideoPath(videoUrl);
        userRepository.save(user);
        logger.debug("User record updated with video path for: {}", username);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Video Uploaded Successfully");
        result.put("videoUrl", videoUrl);
        result.put("verifiedName", user.getFullName() != null ? user.getFullName() : "Verified User");

        return result;
    }
}