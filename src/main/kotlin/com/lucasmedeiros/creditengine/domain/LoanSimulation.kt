package com.lucasmedeiros.creditengine.domain

import java.math.BigDecimal

data class LoanSimulation(
    val totalAmountToBePaid: BigDecimal,
    val monthlyInstallmentAmount: BigDecimal,
    val totalFeePaid: BigDecimal
)
