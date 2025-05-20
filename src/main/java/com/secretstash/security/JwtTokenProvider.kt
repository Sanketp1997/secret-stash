package com.secretstash.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.expiration}")
    private val jwtExpirationInMs: Long
) {
    // Generate a secure key for HS512 algorithm
    private val secretKey: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512)
    
    fun generateToken(username: String): String {
        val claims: Map<String, Any> = HashMap()
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact()
    }
    
    fun getUsernameFromToken(token: String): String {
        val claims: Claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
        
        return claims.subject
    }
    
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getAuthentication(token: String, userDetails: UserDetails): Authentication {
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }
}
