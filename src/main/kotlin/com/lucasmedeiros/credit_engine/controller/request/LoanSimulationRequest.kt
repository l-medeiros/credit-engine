package com.lucasmedeiros.credit_engine.controller.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

data class LoanSimulationRequest(
    @field:Positive(message = "Amount must be positive")
    val amount: Double,
    
    @field:NotNull(message = "Birthdate is required")
    @field:Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Birthdate must be in dd/MM/yyyy format")
    val birthdate: String,
    
    @field:Positive(message = "Number of installments must be positive")
    val installments: Int
)