package com.codewithmajd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DeveloperServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeveloperServiceApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // This bean will be used for hashing the API keys
        return new BCryptPasswordEncoder();
    }
}
