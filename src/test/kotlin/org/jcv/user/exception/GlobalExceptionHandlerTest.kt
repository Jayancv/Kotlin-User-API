package org.jcv.user.exception

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [GlobalExceptionHandlerTest.TestController::class])
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should handle runtime exception`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/runtime-exception"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Runtime exception occurred"))
    }

    @Test
    fun `should handle general exception`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/general-exception"))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Internal server error"))
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