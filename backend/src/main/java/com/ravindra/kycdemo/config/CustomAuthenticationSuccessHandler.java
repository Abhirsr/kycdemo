package com.ravindra.kycdemo.config;

import com.ravindra.kycdemo.model.User;
import com.ravindra.kycdemo.Repo.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.ravindra.kycdemo.service.AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {

            if (user.getVideoPath() != null && !user.getVideoPath().isEmpty()) {

                if (user.getVideoPath() != null && !user.getVideoPath().isEmpty()) {
                    response.sendRedirect("/kyc/success");
                } else if (Boolean.TRUE.equals(user.getIsPanVerified())) {
                    response.sendRedirect("/kyc/video-recording");
                } else {
                    response.sendRedirect("/kyc/verify-pan");
                }
            } else {
                response.sendRedirect("/kyc/verify-pan");
            }
        }
    }
}
