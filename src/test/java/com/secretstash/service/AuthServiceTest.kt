package com.secretstash.service

import com.secretstash.dto.AuthRequest
import com.secretstash.dto.UserRegistrationRequest
import com.secretstash.model.User
import com.secretstash.repository.UserRepository
import com.secretstash.security.JwtTokenProvider
import com.secretstash.security.RateLimitingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class AuthServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val passwordEncoder = mock(PasswordEncoder::class.java)
    private val jwtTokenProvider = mock(JwtTokenProvider::class.java)
    private val authenticationManager = mock(AuthenticationManager::class.java)
    private val rateLimitingService = mock(RateLimitingService::class.java)

    private val authService = AuthService(
        userRepository,
        passwordEncoder,
        jwtTokenProvider,
        authenticationManager,
        rateLimitingService
    )

    @Test
    fun `register should save user and return AuthResponse`() {
        val request = UserRegistrationRequest("user", "Test@Pass111")
        `when`(userRepository.existsByUsername("user")).thenReturn(false)
        `when`(passwordEncoder.encode(anyString())).thenReturn("encodedPass")
        `when`(jwtTokenProvider.generateToken("user")).thenReturn("jwt-token")
        `when`(userRepository.save(any())).thenAnswer { it.arguments[0] }

        val response = authService.register(request)

        assertEquals("jwt-token", response.token)
        assertEquals("user", response.username)
        assertEquals("User registered successfully", response.message)
        verify(userRepository).save(any())
    }

    @Test
    fun `register should throw if username already exists`() {
        val request = UserRegistrationRequest("user", "Test@Pass111")
        `when`(userRepository.existsByUsername("user")).thenReturn(true)

        val ex = assertThrows<IllegalArgumentException> { authService.register(request) }
        assertEquals("Username is already taken", ex.message)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `login should return AuthResponse on success`() {
        val request = AuthRequest("user", "Test@Pass111")
        `when`(rateLimitingService.tryConsume()).thenReturn(true)
        val authentication = mock(Authentication::class.java)
        `when`(authenticationManager.authenticate(any())).thenReturn(authentication)
        `when`(jwtTokenProvider.generateToken("user")).thenReturn("jwt-token")
        val user = User(id = 1, username = "user", password = "encodedPass")
        `when`(userRepository.findByUsername("user")).thenReturn(Optional.of(user))

        val response = authService.login(request)

        assertEquals("jwt-token", response.token)
        assertEquals("user", response.username)
        verify(authenticationManager).authenticate(any())
    }

    @Test
    fun `login should throw on too many attempts`() {
        val request = AuthRequest("user", "Test@Pass111")
        `when`(rateLimitingService.tryConsume()).thenReturn(false)

        val ex = assertThrows<RuntimeException> { authService.login(request) }
        assertTrue(ex.message!!.contains("Too many login attempts"))
        verify(authenticationManager, never()).authenticate(any())
    }

    @Test
    fun `login should throw on bad credentials`() {
        val request = AuthRequest("user", "badpass")
        `when`(rateLimitingService.tryConsume()).thenReturn(true)
        `when`(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException("Bad credentials"))

        val ex = assertThrows<BadCredentialsException> { authService.login(request) }
        assertEquals("Invalid username or password", ex.message)
    }

    @Test
    fun `getCurrentUser should return user`() {
        val user = User(id = 1, username = "user", password = "encodedPass")
        `when`(userRepository.findByUsername("user")).thenReturn(Optional.of(user))
        val authentication = mock(Authentication::class.java)
        `when`(authentication.name).thenReturn("user")
        SecurityContextHolder.getContext().authentication = authentication

        val currentUser = authService.getCurrentUser()

        assertEquals(user, currentUser)
    }
}