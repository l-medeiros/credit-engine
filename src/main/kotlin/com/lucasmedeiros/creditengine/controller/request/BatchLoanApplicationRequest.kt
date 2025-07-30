package com.lucasmedeiros.creditengine.controller.request

import com.lucasmedeiros.creditengine.domain.LoanApplication
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class BatchLoanApplicationRequest(
    @field:Size(min = 1, max = 10000, message = "Batch size must be between 1 and 10000")
    @field:Valid
    val loanApplications: List<LoanApplication>
)
