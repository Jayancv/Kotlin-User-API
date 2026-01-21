package org.jcv.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.jcv.user.dto.UserRequest
import org.jcv.user.dto.UserResponse
import org.jcv.user.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userService: UserService

    private lateinit var userResponse: UserResponse
    private lateinit var userRequest: UserRequest

    @BeforeEach
    fun setUp() {
        userResponse = UserResponse(
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
    fun `getAllUsers should return 200 OK with users list`() {
        // Given
        val users = listOf(userResponse)
        whenever(userService.getAllUsers()).thenReturn(users)

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/users")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Users retrieved successfully"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value(1L))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].firstName").value("John"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].email").value("john.doe@example.com"))

        Mockito.verify(userService, Mockito.times(1)).getAllUsers()
    }

    @Test
    fun `getUserById should return 200 OK when user exists`() {
        // Given
        whenever(userService.getUserById(1L)).thenReturn(userResponse)

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User retrieved successfully"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1L))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.firstName").value("John"))

        Mockito.verify(userService, Mockito.times(1)).getUserById(1L)
    }

    @Test
    fun `getUserById should return 400 when user not found`() {
        // Given
        whenever(userService.getUserById(999L))
            .thenThrow(RuntimeException("User not found with id: 999"))

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/users/999")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not found with id: 999"))

        Mockito.verify(userService, Mockito.times(1)).getUserById(999L)
    }

    @Test
    fun `createUser should return 201 Created when valid request`() {
        // Given
        whenever(userService.createUser(any()))
            .thenReturn(userResponse)

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User created successfully"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1L))

        Mockito.verify(userService, Mockito.times(1)).createUser(any())
    }

    @Test
    fun `createUser should return 400 when validation fails`() {
        // Given
        val invalidRequest = UserRequest(
            firstName = "", // Invalid: empty
            lastName = "D",
            email = "invalid-email",
            phone = null,
            address = null
        )

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Validation failed"))

        Mockito.verify(userService, Mockito.never()).createUser(any())
    }

    @Test
    fun `createUser should return 400 when email already exists`() {
        // Given
        whenever(userService.createUser(any()))
            .thenThrow(RuntimeException("Email already exists: john.doe@example.com"))

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Email already exists: john.doe@example.com"))

        Mockito.verify(userService, Mockito.times(1)).createUser(any())
    }

    @Test
    fun `updateUser should return 200 OK when valid request`() {
        // Given
        whenever(userService.updateUser(ArgumentMatchers.anyLong(), any()))
            .thenReturn(userResponse)

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User updated successfully"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1L))

        Mockito.verify(userService, Mockito.times(1))
            .updateUser(ArgumentMatchers.anyLong(), any())
    }

    @Test
    fun `deleteUser should return 200 OK when user exists`() {
        // Given - doNothing() is default for void methods

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User deleted successfully"))

        Mockito.verify(userService, Mockito.times(1)).deleteUser(1L)
    }

    @Test
    fun `deactivateUser should return 200 OK`() {
        // Given
        val deactivatedUser = userResponse.copy(active = false)
        whenever(userService.deactivateUser(1L)).thenReturn(deactivatedUser)

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/users/1/deactivate")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User deactivated successfully"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.active").value(false))

        Mockito.verify(userService, Mockito.times(1)).deactivateUser(1L)
    }

    @Test
    fun `activateUser should return 200 OK`() {
        // Given
        val activatedUser = userResponse.copy(active = true)
        whenever(userService.activateUser(1L)).thenReturn(activatedUser)

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/users/1/activate")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User activated successfully"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.active").value(true))

        Mockito.verify(userService, Mockito.times(1)).activateUser(1L)
    }

    @Test
    fun `getActiveUsers should return 200 OK with active users`() {
        // Given
        val activeUsers = listOf(userResponse)
        whenever(userService.getActiveUsers()).thenReturn(activeUsers)

        // When & Then
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/users/active")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Active users retrieved successfully"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].active").value(true))

        Mockito.verify(userService, Mockito.times(1)).getActiveUsers()
    }
}