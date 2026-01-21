package org.jcv.user


import com.fasterxml.jackson.databind.ObjectMapper
import org.jcv.user.dto.UserRequest
import org.jcv.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `full CRUD operations should work correctly`() {
        // 1. Create User
        val userRequest = UserRequest(
            firstName = "Integration",
            lastName = "Test",
            email = "integration.test@example.com",
            phone = "555-123-4567",
            address = "Test Address"
        )

        // POST - Create
        val createResponse = mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.firstName").value("Integration"))
            .andExpect(jsonPath("$.data.email").value("integration.test@example.com"))
            .andReturn()

        // Extract created user ID from response
        val responseContent = createResponse.response.contentAsString
        val createdId = objectMapper.readTree(responseContent)
            .path("data").path("id").asLong()

        // 2. GET - Retrieve created user
        mockMvc.perform(get("/api/users/$createdId")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(createdId))
            .andExpect(jsonPath("$.data.active").value(true))

        // 3. GET - All users
        mockMvc.perform(get("/api/users")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(createdId))

        // 4. PUT - Update user
        val updateRequest = UserRequest(
            firstName = "Updated",
            lastName = "User",
            email = "updated.user@example.com",
            phone = "999-888-7777",
            address = "Updated Address"
        )

        mockMvc.perform(put("/api/users/$createdId")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.firstName").value("Updated"))
            .andExpect(jsonPath("$.data.email").value("updated.user@example.com"))

        // 5. PATCH - Deactivate user
        mockMvc.perform(patch("/api/users/$createdId/deactivate")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.active").value(false))

        // 6. GET - Active users (should not include deactivated)
        mockMvc.perform(get("/api/users/active")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isEmpty())

        // 7. PATCH - Activate user
        mockMvc.perform(patch("/api/users/$createdId/activate")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.active").value(true))

        // 8. DELETE - Remove user
        mockMvc.perform(delete("/api/users/$createdId")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        // 9. GET - Verify user is deleted
        mockMvc.perform(get("/api/users/$createdId")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should not create user with duplicate email`() {
        // Given - Create first user
        val userRequest = UserRequest(
            firstName = "First",
            lastName = "User",
            email = "duplicate@example.com",
            phone = "111-222-3333",
            address = "Address 1"
        )

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isCreated)

        // When - Try to create second user with same email
        val secondRequest = UserRequest(
            firstName = "Second",
            lastName = "User",
            email = "duplicate@example.com", // Same email
            phone = "444-555-6666",
            address = "Address 2"
        )

        // Then - Should fail
        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(secondRequest)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email already exists: duplicate@example.com"))
    }

    @Test
    fun `should validate required fields`() {
        val invalidRequest = """
            {
                "firstName": "",
                "lastName": "D",
                "email": "invalid-email"
            }
        """.trimIndent()

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(4))
    }
}