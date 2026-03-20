package com.revpassword.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@lombok.extern.slf4j.Slf4j
public class FallbackController {

    @RequestMapping("/user")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> userServiceFallback() {
        log.error("Fallback triggered: User Service is down");
        return Mono.just(Map.of("message", "User Service is currently down. Please try again later. (Resilience Triggered)"));
    }

    @RequestMapping("/vault")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> vaultServiceFallback() {
        log.error("Fallback triggered: Vault Service is down");
        return Mono.just(Map.of("message", "Vault Service is currently unavailable. Your passwords are safe, but we cannot retrieve them right now."));
    }

    @RequestMapping("/generator")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> generatorServiceFallback() {
        log.error("Fallback triggered: Generator Service is down");
        return Mono.just(Map.of("message", "Password Generator is offline. Please use a manual password for now."));
    }

    @RequestMapping("/security")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> securityServiceFallback() {
        log.error("Fallback triggered: Security Service is down");
        return Mono.just(Map.of("message", "Security Audit service is unavailable. Please check back later for your report."));
    }

    @RequestMapping("/notification")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> notificationServiceFallback() {
        log.error("Fallback triggered: Notification Service is down");
        return Mono.just(Map.of("message", "Notification service is down. In-app alerts may be delayed."));
    }

    @RequestMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Gateway Fallback Controller is Active!");
    }
}
