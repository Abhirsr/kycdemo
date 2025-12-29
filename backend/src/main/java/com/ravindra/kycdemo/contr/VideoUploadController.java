package com.ravindra.kycdemo.contr;

import com.ravindra.kycdemo.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/kyc")
public class VideoUploadController {

    private static final Logger logger = LoggerFactory.getLogger(VideoUploadController.class);

    @Autowired
    private VideoService videoService;

    @PostMapping("/upload-video")
    public ResponseEntity<?> uploadVideo(@RequestParam("video") MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Receiving video upload for user: {}", username);

        try {
            Map<String, Object> result = videoService.processVideoUpload(file, username);
            logger.info("Video upload successful for user: {}", username);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Video upload failed for user: {}", username, e);
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
