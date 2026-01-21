package org.jcv.user.exception

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should handle runtime exception`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/runtime-exception"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Resource not found: test/runtime-exception"))
    }

    @Test
    fun `should handle general exception`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/999"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not find : 999"))
    }

    @RestController
    @RequestMapping("/test")
    class TestController {

        @GetMapping("/runtime-exception")
        fun throwRuntimeException(): String {
            throw RuntimeException("Runtime exception occurred")
        }

        @GetMapping("/general-exception")
        fun throwGeneralException(): String {
            throw Exception("General exception")
        }
    }
}