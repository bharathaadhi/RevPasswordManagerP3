package com.revpassword.apigateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimit extends AbstractGatewayFilterFactory<RateLimit.Config> {

    private final Map<String, UserRateLimit> userRateLimits = new ConcurrentHashMap<>();

    public RateLimit() {
        super(Config.class);
    }

    @Override
    public java.util.List<String> shortcutFieldOrder() {
        return java.util.Arrays.asList("capacity", "refillSeconds");
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            String ip = "unknown";

            // safer IP detection
            String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

            if (forwarded != null) {
                ip = forwarded;
            } else {
                InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
                if (remoteAddress != null && remoteAddress.getAddress() != null) {
                    ip = remoteAddress.getAddress().getHostAddress();
                }
            }

            UserRateLimit rateLimit =
                    userRateLimits.computeIfAbsent(ip,
                            k -> new UserRateLimit(config.getCapacity(), config.getRefillSeconds()));

            if (rateLimit.tryAcquire()) {
                return chain.filter(exchange);
            }

            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        };
    }

    @Data
    public static class Config {
        private int capacity = 10;        // max tokens
        private int refillSeconds = 1;    // refill interval
    }

    private static class UserRateLimit {

        private final int capacity;
        private final int refillSeconds;
        private final AtomicInteger tokens;
        private long lastRefillTime;

        public UserRateLimit(int capacity, int refillSeconds) {
            this.capacity = capacity;
            this.refillSeconds = refillSeconds;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryAcquire() {

            refill();

            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }

            return false;
        }

        private void refill() {

            long now = System.currentTimeMillis();

            if (now - lastRefillTime > refillSeconds * 1000L) {
                tokens.set(capacity);
                lastRefillTime = now;
            }
        }
    }
}