package com.secretstash.security

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.stereotype.Service

@Service
class RateLimitingService(rateLimiterRegistry: RateLimiterRegistry) {
    private val rateLimiter: RateLimiter = rateLimiterRegistry.rateLimiter("default")

    fun tryConsume(): Boolean {
        return rateLimiter.acquirePermission()
    }
}