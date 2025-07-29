package com.lucasmedeiros.creditengine.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val errors: List<FieldError>? = null
)

data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any?
)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.error("Validation error occurred: ${ex.message}")

        val fieldErrors = ex.bindingResult.fieldErrors.map { error ->
            FieldError(
                field = error.field,
                message = error.defaultMessage ?: "Validation error",
                rejectedValue = error.rejectedValue
            )
        }

        val errorResponse = ErrorResponse(
            errors = fieldErrors
        )

        logger.error("Validation error response: $errorResponse")

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}
