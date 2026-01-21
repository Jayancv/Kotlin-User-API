package org.jcv.user.repository


import org.jcv.user.entity.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `findByEmail should return user when email exists`() {
        // Given
        val user = User(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            phone = "123-456-7890",
            address = "Test Address",
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        entityManager.persist(user)
        entityManager.flush()

        // When
        val found = userRepository.findByEmail("test@example.com")

        // Then
        assertTrue(found.isPresent)
        assertEquals("Test", found.get().firstName)
        assertEquals("test@example.com", found.get().email)
    }

    @Test
    fun `findByEmail should return empty when email does not exist`() {
        // When
        val found = userRepository.findByEmail("nonexistent@example.com")

        // Then
        assertFalse(found.isPresent)
    }

    @Test
    fun `existsByEmail should return true when email exists`() {
        // Given
        val user = User(
            firstName = "Test",
            lastName = "User",
            email = "exists@example.com",
            phone = "123-456-7890",
            address = "Test Address",
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        entityManager.persist(user)
        entityManager.flush()

        // When
        val exists = userRepository.existsByEmail("exists@example.com")

        // Then
        assertTrue(exists)
    }

    @Test
    fun `existsByEmail should return false when email does not exist`() {
        // When
        val exists = userRepository.existsByEmail("nonexistent@example.com")

        // Then
        assertFalse(exists)
    }

    @Test
    fun `existsByEmailAndIdNot should return true when email exists for different user`() {
        // Given
        val user1 = User(
            firstName = "User1",
            lastName = "Test",
            email = "user1@example.com",
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val user2 = User(
            firstName = "User2",
            lastName = "Test",
            email = "user2@example.com",
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        entityManager.persist(user1)
        entityManager.persist(user2)
        entityManager.flush()

        // When
        val exists = userRepository.existsByEmailAndIdNot("user2@example.com", user1.id!!)

        // Then
        assertTrue(exists)
    }

    @Test
    fun `existsByEmailAndIdNot should return false when email belongs to same user`() {
        // Given
        val user = User(
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        entityManager.persist(user)
        entityManager.flush()

        // When
        val exists = userRepository.existsByEmailAndIdNot("test@example.com", user.id!!)

        // Then
        assertFalse(exists)
    }

    @Test
    fun `findByActive should return only active users`() {
        // Given
        val activeUser = User(
            firstName = "Active",
            lastName = "User",
            email = "active@example.com",
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val inactiveUser = User(
            firstName = "Inactive",
            lastName = "User",
            email = "inactive@example.com",
            active = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        entityManager.persist(activeUser)
        entityManager.persist(inactiveUser)
        entityManager.flush()

        // When
        val activeUsers = userRepository.findByActive(true)
        val inactiveUsers = userRepository.findByActive(false)

        // Then
        assertEquals(1, activeUsers.size)
        assertEquals("active@example.com", activeUsers[0].email)
        
        assertEquals(1, inactiveUsers.size)
        assertEquals("inactive@example.com", inactiveUsers[0].email)
    }

    @Test
    fun `should save and retrieve user with all fields`() {
        // Given
        val user = User(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "123-456-7890",
            address = "123 Main St",
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        val saved = userRepository.save(user)
        val found = userRepository.findById(saved.id!!)

        // Then
        assertTrue(found.isPresent)
        assertEquals("John", found.get().firstName)
        assertEquals("Doe", found.get().lastName)
        assertEquals("john.doe@example.com", found.get().email)
        assertEquals("123-456-7890", found.get().phone)
        assertEquals("123 Main St", found.get().address)
        assertTrue(found.get().active)
        assertNotNull(found.get().createdAt)
        assertNotNull(found.get().updatedAt)
    }
}