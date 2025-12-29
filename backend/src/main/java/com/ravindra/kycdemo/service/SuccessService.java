package com.ravindra.kycdemo.service;

import com.ravindra.kycdemo.Repo.UserRepository;
import com.ravindra.kycdemo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SuccessService {

    @Autowired
    private UserRepository userRepository;

    public User getUserData(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public Resource getVideoResource(String username) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Path path = Paths.get(user.getVideoPath());
        return new UrlResource(path.toUri());
    }
}