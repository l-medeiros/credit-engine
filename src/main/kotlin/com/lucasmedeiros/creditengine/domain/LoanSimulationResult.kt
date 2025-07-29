package com.lucasmedeiros.creditengine.domain

import java.math.BigDecimal

data class LoanSimulationResult(
    val totalAmountToBePaid: BigDecimal,
    val monthlyInstallmentAmount: BigDecimal,
    val totalFeePaid: BigDecimal
)
