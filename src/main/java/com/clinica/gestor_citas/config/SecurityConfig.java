package com.clinica.gestor_citas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

    @Configuration
    public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/usuarios/login", "/api/usuarios/registro").permitAll()
                            .anyRequest().authenticated()
                    )
                    .formLogin(form -> form.disable())
                    .httpBasic(basic -> basic.disable());

            return http.build();
        }
    }

