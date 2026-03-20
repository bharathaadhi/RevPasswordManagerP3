package com.revpassword.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        // Rate limit by IP address with fallback to "anonymous"
        return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress() != null && 
            exchange.getRequest().getRemoteAddress().getAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "anonymous"
        );
    }
}
