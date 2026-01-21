package org.jcv.user.exception

import org.jcv.user.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<*>> {
        val errors = ex.bindingResult.allErrors.map { error ->
            if (error is FieldError) {
                "${error.field}: ${error.defaultMessage}"
            } else {
                error.defaultMessage ?: "Validation error"
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse(
                success = false,
                message = "Validation failed",
                data = null,
                errors = errors
            )
        )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ApiResponse<*>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse(
                success = false,
                message = "Resource not found: ${ex.resourcePath}",
                data = null
            )
        )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiResponse<*>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse(
                success = false,
                message = ex.message ?: "An error occurred",
                data = null
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ApiResponse<*>> {
        // Log for debugging
        println("Unhandled exception: ${ex.javaClass.name} - ${ex.message}")

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse(
                success = false,
                message = "Internal server error",
                data = null
            )
        )
    }
}