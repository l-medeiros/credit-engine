package com.lucasmedeiros.creditengine.controller.request

import com.lucasmedeiros.creditengine.domain.LoanApplication
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

data class LoanApplicationRequest(
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,

    @field:NotNull(message = "Birthdate is required")
    @field:Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "Birthdate must be in dd/MM/yyyy format")
    val birthdate: String,

    @field:Positive(message = "Number of installments must be positive")
    val installments: Int
) {

    fun toDomain(): LoanApplication {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val localDate = LocalDate.parse(birthdate, formatter)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())

        return LoanApplication(
            amount = amount,
            birthdate = date,
            installments = installments
        )
    }
}
