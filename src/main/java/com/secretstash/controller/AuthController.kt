package com.secretstash.controller

import com.secretstash.dto.AuthRequest
import com.secretstash.dto.AuthResponse
import com.secretstash.dto.UserRegistrationRequest
import com.secretstash.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "The Authentication API")
class AuthController(private val authService: AuthService) {

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided credentials"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User registered successfully",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid input or username already taken")
        ]
    )
    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody request: UserRegistrationRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a user and returns a JWT token"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", 
                description = "Authentication successful",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid credentials"),
            ApiResponse(responseCode = "429", description = "Too many login attempts")
        ]
    )
    @PostMapping("/login")
    fun authenticateUser(@Valid @RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
}
