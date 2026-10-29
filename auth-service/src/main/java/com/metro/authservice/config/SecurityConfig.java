package com.metro.authservice.config;

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

    // CHANGED: the success handler is now a method parameter (Spring injects it)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            OAuth2LoginSuccessHandler oauth2SuccessHandler) throws Exception {
        // NOTE: the Spring Security DSL is built around lambdas, so this is the one place
        // I kept them (your original code already did). Everything else is lambda-free.
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // NEW: this is what switches Google login ON and connects your success handler.
            // Without it, OAuth2LoginSuccessHandler was never called.
            .oauth2Login(oauth -> oauth.successHandler(oauth2SuccessHandler));
        return http.build();
    }
}