package com.codegym.Quiz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tạm thời disable CSRF để submit form dễ dàng
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Cho phép truy cập tất cả đường dẫn không cần login
                )
                .formLogin(form -> form.disable()); // Tắt trang đăng nhập mặc định của Spring Security

        return http.build();
    }
}