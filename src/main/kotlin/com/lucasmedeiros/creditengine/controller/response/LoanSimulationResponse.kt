package com.lucasmedeiros.creditengine.controller.response

import com.lucasmedeiros.creditengine.domain.LoanSimulationResult
import java.math.BigDecimal

data class LoanSimulationResponse(
    val totalAmount: BigDecimal,
    val installmentAmount: BigDecimal,
    val totalFee: BigDecimal
) {
    companion object {
        fun fromDomain(result: LoanSimulationResult): LoanSimulationResponse {
            return LoanSimulationResponse(
                totalAmount = result.totalAmountToBePaid,
                installmentAmount = result.monthlyInstallmentAmount,
                totalFee = result.totalFeePaid
            )
        }
    }
}
