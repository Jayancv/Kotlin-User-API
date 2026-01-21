package org.jcv.user.service

import jakarta.transaction.Transactional
import org.jcv.user.dto.UserRequest
import org.jcv.user.dto.UserResponse
import org.jcv.user.entity.User
import org.jcv.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toResponse() }
    }

    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow { RuntimeException("User not find : $id") };
        return user.toResponse();
    }

    @Transactional
    fun createUser(request: UserRequest): UserResponse {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw RuntimeException("Email already exists: ${request.email}")
        }

        val user = User(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phone = request.phone,
            address = request.address
        )

        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }


    @Transactional
    fun updateUser(id: Long, request: UserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("User not found with id: $id") }

        // Check if email exists for another user
        if (userRepository.existsByEmailAndIdNot(request.email, id)) {
            throw RuntimeException("Email already exists: ${request.email}")
        }

        user.apply {
            firstName = request.firstName
            lastName = request.lastName
            email = request.email
            phone = request.phone
            address = request.address
        }

        val updatedUser = userRepository.save(user)
        return updatedUser.toResponse()
    }

    @Transactional
    fun deleteUser(id: Long) {
        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("User not found with id: $id") }
        userRepository.delete(user)
    }

    @Transactional
    fun deactivateUser(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("User not found with id: $id") }
        user.active = false
        val updatedUser = userRepository.save(user)
        return updatedUser.toResponse()
    }

    @Transactional
    fun activateUser(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("User not found with id: $id") }
        user.active = true
        val updatedUser = userRepository.save(user)
        return updatedUser.toResponse()
    }

    fun getActiveUsers(): List<UserResponse> {
        return userRepository.findByActive(true).map { it.toResponse() }
    }

    private fun User.toResponse(): UserResponse {
        return UserResponse(
            id = this.id!!,
            firstName = this.firstName,
            lastName = this.lastName,
            email = this.email,
            phone = this.phone,
            address = this.address,
            active = this.active,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}