package org.jcv.user.controller

import jakarta.validation.Valid
import org.jcv.user.dto.ApiResponse
import org.jcv.user.dto.UserRequest
import org.jcv.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers(): ResponseEntity<ApiResponse<List<*>>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Users retrieved successfully",
                data = users
            )
        )
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<*>> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "User retrieved successfully",
                data = user
            )
        )
    }

    @PostMapping
    fun createUser(@Valid @RequestBody request: UserRequest): ResponseEntity<ApiResponse<*>> {
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "User created successfully",
                data = user
            )
        )
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserRequest
    ): ResponseEntity<ApiResponse<*>> {
        val user = userService.updateUser(id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "User updated successfully",
                data = user
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<ApiResponse<*>> {
        userService.deleteUser(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "User deleted successfully",
                data = null
            )
        )
    }

    @PatchMapping("/{id}/deactivate")
    fun deactivateUser(@PathVariable id: Long): ResponseEntity<ApiResponse<*>> {
        val user = userService.deactivateUser(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "User deactivated successfully",
                data = user
            )
        )
    }

    @PatchMapping("/{id}/activate")
    fun activateUser(@PathVariable id: Long): ResponseEntity<ApiResponse<*>> {
        val user = userService.activateUser(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "User activated successfully",
                data = user
            )
        )
    }

    @GetMapping("/active")
    fun getActiveUsers(): ResponseEntity<ApiResponse<List<*>>> {
        val users = userService.getActiveUsers()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Active users retrieved successfully",
                data = users
            )
        )
    }
}