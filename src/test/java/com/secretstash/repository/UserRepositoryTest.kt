package com.secretstash.repository

import com.secretstash.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class UserRepositoryTest @Autowired constructor(
    val userRepository: UserRepository
) {

    @Test
    fun `findByUsername returns user if exists`() {
        val user = userRepository.save(User(username = "uniqueUser", password = "pass"))
        val found = userRepository.findByUsername("uniqueUser")
        assertTrue(found.isPresent)
        assertEquals(user, found.get())
    }

    @Test
    fun `existsByUsername returns true if user exists`() {
        userRepository.save(User(username = "existsUser", password = "pass"))
        assertTrue(userRepository.existsByUsername("existsUser"))
    }

    @Test
    fun `existsByUsername returns false if user does not exist`() {
        assertFalse(userRepository.existsByUsername("doesNotExist"))
    }
}