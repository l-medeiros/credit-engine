package com.lucasmedeiros.creditengine.service

import com.lucasmedeiros.creditengine.domain.LoanApplication
import com.lucasmedeiros.creditengine.domain.LoanSimulation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@Service
class SimulationService(private val feeService: FeeService) {

    companion object {
        private val MATH_CONTEXT = MathContext(10, RoundingMode.HALF_UP)
        private const val MATH_SCALE = 2
    }

    private val logger = LoggerFactory.getLogger(SimulationService::class.java)

    fun simulate(simulation: LoanApplication): LoanSimulation {
        logger.info("Starting loan simulation for $simulation")

        val feeRate = feeService.calculateFeeRate(simulation.birthdate)

        val installmentAmount = calculateInstallmentAmount(
            simulation.amount,
            simulation.installments,
            BigDecimal.valueOf(feeRate)
        )

        val totalAmount = installmentAmount.multiply(BigDecimal.valueOf(simulation.installments.toLong()))
        val totalFeePaid = totalAmount.subtract(simulation.amount)

        return LoanSimulation(
            totalAmountToBePaid = totalAmount,
            monthlyInstallmentAmount = installmentAmount,
            totalFeePaid = totalFeePaid
        ).also { logger.info("Simulation completed - result: $it") }
    }

    private fun calculateInstallmentAmount(amount: BigDecimal, installments: Int, feeRate: BigDecimal): BigDecimal {
        logger.info("Calculating installment amount for: amount=$amount, installments=$installments, feeRate=$feeRate")

        val monthlyFeeRate = feeRate.divide(BigDecimal.valueOf(12), MATH_CONTEXT)
        logger.info("monthlyFeeRate=$monthlyFeeRate")

        val onePlusRate = BigDecimal.ONE.add(monthlyFeeRate)
        val denominator = BigDecimal.ONE.subtract(
            onePlusRate.pow(-installments, MATH_CONTEXT)
        )

        return amount
            .multiply(monthlyFeeRate)
            .divide(denominator, MATH_CONTEXT)
            .setScale(MATH_SCALE, RoundingMode.HALF_UP)
    }
}
