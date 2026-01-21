package org.jcv.user.service

import org.jcv.user.dto.UserRequest
import org.jcv.user.dto.UserResponse
import org.jcv.user.entity.User
import org.jcv.user.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(SpringExtension::class)
class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService
    private lateinit var mockUser: User
    private lateinit var userRequest: UserRequest

    @BeforeEach
    fun setUp() {
        userRepository = Mockito.mock()
        userService = UserService(userRepository)

        mockUser = User(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "123-456-7890",
            address = "123 Main St",
            active = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        userRequest = UserRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "123-456-7890",
            address = "123 Main St"
        )
    }

    @Test
    fun `getAllUsers should return list of users`() {
        // Given
        val users = listOf(mockUser)
        whenever(userRepository.findAll()).thenReturn(users)

        // When
        val result = userService.getAllUsers()

        // Then
        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals("John", result[0].firstName)
        Assertions.assertEquals("Doe", result[0].lastName)
        Mockito.verify(userRepository, Mockito.times(1)).findAll()
    }

    @Test
    fun `getUserById should return user when user exists`() {
        // Given
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(mockUser))

        // When
        val result = userService.getUserById(1L)

        // Then
        Assertions.assertEquals("John", result.firstName)
        Assertions.assertEquals("john.doe@example.com", result.email)
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L)
    }

    @Test
    fun `getUserById should throw exception when user not found`() {
        // Given
        whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

        // When & Then
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            userService.getUserById(999L)
        }
        Assertions.assertEquals("User not find : 999", exception.message)
    }

    @Test
    fun `createUser should save and return user`() {
        // Given
        whenever(userRepository.existsByEmail(ArgumentMatchers.anyString())).thenReturn(false)
        whenever(userRepository.save(ArgumentMatchers.any(User::class.java))).thenReturn(mockUser)

        // When
        val result = userService.createUser(userRequest)

        // Then
        Assertions.assertEquals("John", result.firstName)
        Assertions.assertEquals("Doe", result.lastName)
        Mockito.verify(userRepository, Mockito.times(1)).existsByEmail("john.doe@example.com")
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `createUser should throw exception when email already exists`() {
        // Given
        whenever(userRepository.existsByEmail(ArgumentMatchers.anyString())).thenReturn(true)

        // When & Then
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            userService.createUser(userRequest)
        }
        Assertions.assertEquals("Email already exists: john.doe@example.com", exception.message)
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `updateUser should update and return user`() {
        // Given
        val updatedRequest = UserRequest(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com",
            phone = "987-654-3210",
            address = "456 Oak Ave"
        )

        whenever(userRepository.findById(1L)).thenReturn(Optional.of(mockUser))
        whenever(userRepository.existsByEmailAndIdNot(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())).thenReturn(false)
        whenever(userRepository.save(ArgumentMatchers.any(User::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0) as User
        }

        // When
        val result = userService.updateUser(1L, updatedRequest)

        // Then
        Assertions.assertEquals("Jane", result.firstName)
        Assertions.assertEquals("Smith", result.lastName)
        Assertions.assertEquals("jane.smith@example.com", result.email)
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L)
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `updateUser should throw exception when email exists for another user`() {
        // Given
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(mockUser))
        whenever(userRepository.existsByEmailAndIdNot(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())).thenReturn(true)

        // When & Then
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            userService.updateUser(1L, userRequest)
        }
        Assertions.assertEquals("Email already exists: john.doe@example.com", exception.message)
        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `deleteUser should delete user when user exists`() {
        // Given
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(mockUser))

        // When
        userService.deleteUser(1L)

        // Then
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L)
        Mockito.verify(userRepository, Mockito.times(1)).delete(mockUser)
    }

    @Test
    fun `deleteUser should throw exception when user not found`() {
        // Given
        whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

        // When & Then
        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            userService.deleteUser(999L)
        }
        Assertions.assertEquals("User not found with id: 999", exception.message)
        Mockito.verify(userRepository, Mockito.never()).delete(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `deactivateUser should deactivate user`() {
        // Given
        val activeUser = mockUser.copy(active = true)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(activeUser))
        whenever(userRepository.save(ArgumentMatchers.any(User::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0) as User
        }

        // When
        val result = userService.deactivateUser(1L)

        // Then
        assertFalse(result.active)
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `activateUser should activate user`() {
        // Given
        val inactiveUser = mockUser.copy(active = false)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(inactiveUser))
        whenever(userRepository.save(ArgumentMatchers.any(User::class.java))).thenAnswer { invocation ->
            invocation.getArgument(0) as User
        }

        // When
        val result = userService.activateUser(1L)

        // Then
        assertTrue(result.active)
        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User::class.java))
    }

    @Test
    fun `getActiveUsers should return only active users`() {
        // Given
        val activeUser = mockUser.copy(active = true)
        val inactiveUser = mockUser.copy(id = 2L, active = false)
        whenever(userRepository.findByActive(true)).thenReturn(listOf(activeUser))

        // When
        val result = userService.getActiveUsers()

        // Then
        Assertions.assertEquals(1, result.size)
        assertTrue(result[0].active)
        Mockito.verify(userRepository, Mockito.times(1)).findByActive(true)
    }

    @Test
    fun `toResponse should convert user to response DTO`() {
        // Given
        val user = mockUser

        // When (using internal function)
        val result = user.run {
            UserResponse(
                id = id!!,
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                address = address,
                active = active,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }

        // Then
        assertEquals(user.id, result.id)
        assertEquals(user.firstName, result.firstName)
        assertEquals(user.email, result.email)
    }
}