package com.lucasmedeiros.creditengine.domain

import java.math.BigDecimal
import java.util.Date

data class LoanSimulation(
    val amount: BigDecimal,
    val birthdate: Date,
    val installments: Int
)
