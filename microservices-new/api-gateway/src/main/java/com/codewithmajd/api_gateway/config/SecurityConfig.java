package com.codewithmajd.api_gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {

    @PostConstruct
    public void loaded() {
        log.info("✅ SecurityConfig has been loaded with active profile: {}", System.getProperty("spring.profiles.active"));
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorize -> authorize
                        .pathMatchers("/api/public/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        log.info("✅ SecurityConfig has been loaded.");
        return http.build();
    }

    @Bean
    @Order(1) // Run this filter AFTER security filters.
    public GlobalFilter tenantIdInjectionFilter() {
        return (exchange, chain) -> {
            log.trace("Tenant ID injection filter triggered.");
            return exchange.getPrincipal()
                    .filter(principal -> principal instanceof JwtAuthenticationToken)
                    .cast(JwtAuthenticationToken.class)
                    .flatMap(token -> {
                        Jwt jwt = token.getToken();
                        String tenantId = jwt.getSubject();
                        log.info("🔐 Injecting Tenant-ID: {}", tenantId);

                        var mutatedRequest = exchange.getRequest().mutate()
                                .header("X-Tenant-ID", tenantId)
                                .build();
                        var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                        return chain.filter(mutatedExchange);
                    })
                    .switchIfEmpty(chain.filter(exchange));
        };
    }
}