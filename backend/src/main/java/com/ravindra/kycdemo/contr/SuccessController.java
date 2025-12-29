package com.ravindra.kycdemo.contr;

import com.ravindra.kycdemo.model.User;
import com.ravindra.kycdemo.service.SuccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class SuccessController {

    @Autowired
    private SuccessService successService;

    @GetMapping("/kyc/success")
    public String showSuccess(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = successService.getUserData(username);
        model.addAttribute("user", user);
        return "success";
    }

    @GetMapping("/kyc/video-stream/{username}")
    @ResponseBody
    public ResponseEntity<Resource> streamVideo(@PathVariable String username) throws IOException {
        Resource video = successService.getVideoResource(username);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/webm"))
                .body(video);
    }
}