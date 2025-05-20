package com.secretstash.service

import com.secretstash.dto.AuthRequest
import com.secretstash.dto.AuthResponse
import com.secretstash.dto.UserRegistrationRequest
import com.secretstash.model.User
import com.secretstash.repository.UserRepository
import com.secretstash.security.JwtTokenProvider
import com.secretstash.security.RateLimitingService
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val rateLimitingService: RateLimitingService
) {

    fun register(request: UserRegistrationRequest): AuthResponse {
        require(!userRepository.existsByUsername(request.username)) { "Username is already taken" }

        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password)
        )

        userRepository.save(user)

        val token = jwtTokenProvider.generateToken(user.username)
        return AuthResponse(token, user.username, "User registered successfully")
    }

    fun login(request: AuthRequest): AuthResponse {

        // Apply rate limiting
        if (!rateLimitingService.tryConsume()) {
            throw ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many login attempts. Please try again later."
            )
        }

        try {
            val authentication: Authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            // Update last login time
            userRepository.findByUsername(request.username).ifPresent { user ->
                user.lastLogin = LocalDateTime.now()
                userRepository.save(user)
            }

            val token = jwtTokenProvider.generateToken(request.username)
            return AuthResponse(token, request.username)
        } catch (e: BadCredentialsException) {
            throw BadCredentialsException("Invalid username or password")
        }
    }

    fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name
        return userRepository.findByUsername(username)
            .orElseThrow { IllegalStateException("Current user not found") }
    }
}
