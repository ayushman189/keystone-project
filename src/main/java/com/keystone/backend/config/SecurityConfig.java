package com.keystone.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.keystone.backend.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/reports/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/api/parts/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/api/customers/**").hasAnyRole("MANAGER", "ADMIN", "DISPATCHER")
                .requestMatchers("/api/sites/**").hasAnyRole("MANAGER", "ADMIN", "DISPATCHER")
                .requestMatchers("/api/notifications/**").hasAnyRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN")
                .requestMatchers("/api/work-orders/**").hasAnyRole("MANAGER", "ADMIN", "DISPATCHER", "TECHNICIAN")
                .requestMatchers("/api/my-work-orders/**").hasAnyRole("CUSTOMER", "TECHNICIAN", "DISPATCHER", "MANAGER", "ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}