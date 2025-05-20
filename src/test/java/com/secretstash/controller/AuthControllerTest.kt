package com.secretstash.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.secretstash.dto.AuthRequest
import com.secretstash.dto.AuthResponse
import com.secretstash.dto.UserRegistrationRequest
import com.secretstash.security.JwtTokenProvider
import com.secretstash.security.RateLimitingService
import com.secretstash.service.AuthService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException

@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var authService: AuthService

    @MockBean
    lateinit var rateLimitingService: RateLimitingService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `registerUser returns 201 on success`() {
        val request = UserRegistrationRequest("user", "Test@Pass111")
        val response = AuthResponse("jwt-token", "test_user")
        `when`(authService.register(request)).thenReturn(response)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.token").value("jwt-token"))
    }

    @Test
    fun `registerUser returns 400 on invalid input`() {
        val request = UserRegistrationRequest("", "") // invalid

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `authenticateUser returns 200 on success`() {
        val request = AuthRequest("user", "pass")
        val response = AuthResponse("jwt-token", "test_user")
        whenever(authService.login(eq(request))).thenReturn(response)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt-token"))
    }

    @Test
    fun `authenticateUser returns 400 on invalid credentials`() {
        val request = AuthRequest("user", "badpass")
        whenever(authService.login(eq(request))).thenThrow(IllegalArgumentException("Invalid credentials"))

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `authenticateUser returns 429 on too many attempts`() {
        val request = AuthRequest("user", "pass")
        whenever(authService.login(eq(request))).thenThrow(
            ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts. Please try again later.")
        )
        `when`(rateLimitingService.tryConsume()).thenReturn(false)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isTooManyRequests)
    }


    @TestConfiguration
    class TestSecurityConfig {
        @Bean
        fun webSecurityCustomizer(): WebSecurityCustomizer {
            return WebSecurityCustomizer { web: WebSecurity ->
                web.ignoring().anyRequest()
            }
        }
    }
}