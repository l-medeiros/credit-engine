package com.lucasmedeiros.creditengine.controller.response

import com.lucasmedeiros.creditengine.domain.LoanSimulation
import java.math.BigDecimal

data class LoanSimulationResponse(
    val totalAmount: BigDecimal,
    val installmentAmount: BigDecimal,
    val totalFee: BigDecimal
) {
    companion object {
        fun fromDomain(result: LoanSimulation): LoanSimulationResponse {
            return LoanSimulationResponse(
                totalAmount = result.totalAmountToBePaid,
                installmentAmount = result.monthlyInstallmentAmount,
                totalFee = result.totalFeePaid
            )
        }
    }
}
