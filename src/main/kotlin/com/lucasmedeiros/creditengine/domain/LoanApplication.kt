package com.lucasmedeiros.creditengine.domain

import java.math.BigDecimal

data class LoanApplication(
    val amount: BigDecimal,
    val birthdate: String,
    val installments: Int
)
