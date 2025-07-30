package com.lucasmedeiros.creditengine.service

import com.lucasmedeiros.creditengine.domain.LoanApplication
import com.lucasmedeiros.creditengine.domain.LoanSimulation
import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationEntity
import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationStatus
import com.lucasmedeiros.creditengine.infra.jpa.repository.SimulationRepository
import com.lucasmedeiros.creditengine.infra.jpa.repository.BatchSimulationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class SimulationService(
    private val feeService: FeeService,
    private val simulationRepository: SimulationRepository,
    private val batchSimulationRepository: BatchSimulationRepository
) {

    companion object {
        private val MATH_CONTEXT = MathContext(10, RoundingMode.HALF_UP)
        private const val MATH_SCALE = 2
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    private val logger = LoggerFactory.getLogger(SimulationService::class.java)

    fun simulate(loanApplication: LoanApplication): LoanSimulation {
        logger.info("Starting loan simulation for $loanApplication")

        val feeRate = feeService.calculateFeeRate(loanApplication.birthdate)

        val installmentAmount = calculateInstallmentAmount(
            loanApplication.amount,
            loanApplication.installments,
            BigDecimal.valueOf(feeRate)
        )

        val totalAmount = installmentAmount.multiply(BigDecimal.valueOf(loanApplication.installments.toLong()))
        val totalFeePaid = totalAmount.subtract(loanApplication.amount)

        return LoanSimulation(
            totalAmountToBePaid = totalAmount,
            monthlyInstallmentAmount = installmentAmount,
            totalFeePaid = totalFeePaid
        ).also { logger.info("Simulation completed - result: $it") }
    }

    @Transactional
    fun saveSuccessfulSimulation(
        batchId: UUID,
        loanApplication: LoanApplication,
        result: LoanSimulation
    ): SimulationEntity {
        val simulationEntity = SimulationEntity(
            status = SimulationStatus.COMPLETED,
            amountRequested = loanApplication.amount,
            birthdate = LocalDate.parse(loanApplication.birthdate, DATE_FORMATTER),
            installments = loanApplication.installments,
            totalAmount = result.totalAmountToBePaid,
            installmentAmount = result.monthlyInstallmentAmount,
            totalFee = result.totalFeePaid,
            processedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            batchId = batchId
        )

        return simulationRepository.save(simulationEntity).also {
            logger.info("Successful simulation saved for batchId=$batchId")
        }
    }

    @Transactional
    fun saveFailedSimulation(
        batchId: UUID,
        loanApplication: LoanApplication,
        error: Exception
    ): SimulationEntity {
        val simulationEntity = SimulationEntity(
            batchId = batchId,
            status = SimulationStatus.FAILED,
            amountRequested = loanApplication.amount,
            birthdate = LocalDate.parse(loanApplication.birthdate, DATE_FORMATTER),
            installments = loanApplication.installments,
            processedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        return simulationRepository.save(simulationEntity).also {
            logger.error("Failed simulation saved for batchId=$batchId: ${error.message}")
        }
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
