package com.revpassword.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user")
    public Mono<String> userServiceFallback() {
        return Mono.just("User Service is taking too long to respond or is down. Please try again later.");
    }

    @GetMapping("/vault")
    public Mono<String> vaultServiceFallback() {
        return Mono.just("Vault Service is currently unavailable. Your passwords are safe, but we cannot retrieve them right now.");
    }

    @GetMapping("/generator")
    public Mono<String> generatorServiceFallback() {
        return Mono.just("Password Generator is offline. Please use a manual password for now.");
    }

    @GetMapping("/security")
    public Mono<String> securityServiceFallback() {
        return Mono.just("Security Audit service is unavailable. Please check back later for your report.");
    }

    @GetMapping("/notification")
    public Mono<String> notificationServiceFallback() {
        return Mono.just("Notification service is down. In-app alerts may be delayed.");
    }
}
