package com.example.demo.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId).orElse(null);

        if (user != null) {
            // 1. mustChangePasswordフラグチェック
            if (user.isMustChangePassword()) {
                response.sendRedirect("/password/change");
                return;
            }

            // 2. デフォルトパスワード"password"チェック
            if (passwordEncoder.matches("password", user.getPassword())) {
                response.sendRedirect("/password/change");
                return;
            }
        }

        // 通常のホーム画面へ
        response.sendRedirect("/home");
    }
}
