package com.codegym.Quiz.config;

import com.codegym.Quiz.authentication.security.CustomUserDetailsService;
import com.codegym.Quiz.authentication.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          PasswordEncoder passwordEncoder,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Kết nối UserDetailsService và PasswordEncoder vào Spring Security.
     * Spring Boot 4.x: DaoAuthenticationProvider yêu cầu truyền UserDetailsService qua constructor.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Expose AuthenticationManager để AuthService có thể inject khi xác thực login JWT.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cấu hình Security cho WEB (Thymeleaf + Form Login + Session).
     * JWT filter chỉ chạy cho /api/** → xem JwtAuthenticationFilter.shouldNotFilter()
     */
    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())

            // ─── Phân quyền URL ───────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên công khai – ai cũng truy cập được
                .requestMatchers(
                    "/",
                    "/dashboard",
                    "/auth/login",
                    "/auth/register",
                    "/user/login",
                    "/user/register",
                    "/user/forgot-password",
                    "/user/reset-password",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico"
                ).permitAll()
                // REST API auth endpoints – public để login/register qua API
                .requestMatchers("/api/auth/**").permitAll()
                // Chỉ ADMIN mới vào /admin/**
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Chỉ TEACHER hoặc ADMIN mới vào /teacher/**
                .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN")
                // Còn lại phải đăng nhập (ROLE_USER, ROLE_STUDENT, ROLE_ADMIN đều được)
                .anyRequest().authenticated()
            )

            // ─── Form Login (Web – Session based) ─────────────────────────────
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )

            // ─── Logout ───────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .clearAuthentication(true)
                .permitAll()
            )

            // ─── Remember Me (30 ngày) ────────────────────────────────────────
            .rememberMe(remember -> remember
                .key("QuizApp_RememberMe_Secret_2026")
                .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 ngày
                .userDetailsService(customUserDetailsService)
                .rememberMeParameter("remember-me")
            )

            // ─── CSRF: bật cho web, tắt cho /api/** ──────────────────────────
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )

            // ─── Session Management ───────────────────────────────────────────
            // Web dùng session (IF_REQUIRED); JWT filter tự quản lý cho /api/**
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // ─── JWT Filter cho /api/** ───────────────────────────────────────
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
