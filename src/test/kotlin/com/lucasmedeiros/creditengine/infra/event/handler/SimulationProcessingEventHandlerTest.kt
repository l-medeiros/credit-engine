package com.lucasmedeiros.creditengine.infra.event.handler

import com.lucasmedeiros.creditengine.domain.LoanApplication
import com.lucasmedeiros.creditengine.domain.LoanSimulation
import com.lucasmedeiros.creditengine.domain.event.SimulationProcessingEvent
import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationEntity
import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationStatus
import com.lucasmedeiros.creditengine.service.BatchSimulationService
import com.lucasmedeiros.creditengine.service.SimulationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SimulationProcessingEventHandlerTest {

    private lateinit var simulationService: SimulationService
    private lateinit var batchSimulationService: BatchSimulationService
    private lateinit var handler: SimulationProcessingEventHandler

    @BeforeEach
    fun setUp() {
        simulationService = mockk<SimulationService>()
        batchSimulationService = mockk<BatchSimulationService>()
        handler = SimulationProcessingEventHandler(simulationService, batchSimulationService)
    }

    @Test
    fun `should handle successful simulation processing`() {
        val batchId = UUID.randomUUID()
        val loanApplication = LoanApplication(
            amount = BigDecimal("10000.00"),
            birthdate = "15/03/1990",
            installments = 12
        )
        val event = SimulationProcessingEvent(batchId, loanApplication)

        val loanSimulation = LoanSimulation(
            monthlyInstallmentAmount = BigDecimal("900.00"),
            totalAmountToBePaid = BigDecimal("10800.00"),
            totalFeePaid = BigDecimal("800.00")
        )

        val savedEntity = SimulationEntity(
            id = UUID.randomUUID(),
            status = SimulationStatus.COMPLETED,
            amountRequested = loanApplication.amount,
            birthdate = LocalDate.of(1990, 3, 15),
            installments = loanApplication.installments,
            totalAmount = loanSimulation.totalAmountToBePaid,
            installmentAmount = loanSimulation.monthlyInstallmentAmount,
            totalFee = loanSimulation.totalFeePaid,
            processedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { simulationService.simulate(loanApplication) } returns loanSimulation
        every {
            simulationService.saveSuccessfulSimulation(batchId, loanApplication, loanSimulation)
        } returns savedEntity
        every { batchSimulationService.incrementCompletedSimulations(batchId) } just runs

        handler.handle(event)

        verify { simulationService.simulate(loanApplication) }
        verify { simulationService.saveSuccessfulSimulation(batchId, loanApplication, loanSimulation) }
        verify { batchSimulationService.incrementCompletedSimulations(batchId) }
    }

    @Test
    fun `should handle failed simulation processing`() {
        val batchId = UUID.randomUUID()
        val loanApplication = LoanApplication(
            amount = BigDecimal("10000.00"),
            birthdate = "15/03/1990",
            installments = 12
        )
        val event = SimulationProcessingEvent(batchId, loanApplication)
        val exception = RuntimeException("Simulation failed")

        val savedEntity = SimulationEntity(
            id = UUID.randomUUID(),
            status = SimulationStatus.FAILED,
            amountRequested = loanApplication.amount,
            birthdate = LocalDate.of(1990, 3, 15),
            installments = loanApplication.installments,
            processedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { simulationService.simulate(loanApplication) } throws exception
        every { simulationService.saveFailedSimulation(batchId, loanApplication, exception) } returns savedEntity
        every { batchSimulationService.incrementFailedSimulations(batchId) } just runs

        handler.handle(event)

        verify { simulationService.simulate(loanApplication) }
        verify { simulationService.saveFailedSimulation(batchId, loanApplication, exception) }
        verify { batchSimulationService.incrementFailedSimulations(batchId) }
    }
}
