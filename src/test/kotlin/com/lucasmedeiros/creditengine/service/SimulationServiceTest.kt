package com.lucasmedeiros.creditengine.service

import com.lucasmedeiros.creditengine.domain.LoanApplication
import com.lucasmedeiros.creditengine.domain.LoanSimulation
import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationEntity
import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationStatus
import com.lucasmedeiros.creditengine.infra.jpa.repository.BatchSimulationRepository
import com.lucasmedeiros.creditengine.infra.jpa.repository.SimulationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

class SimulationServiceTest {

    private lateinit var feeService: FeeService
    private lateinit var simulationRepository: SimulationRepository
    private lateinit var batchSimulationRepository: BatchSimulationRepository
    private lateinit var simulationService: SimulationService

    @BeforeEach
    fun setUp() {
        feeService = mockk<FeeService>()
        simulationRepository = mockk<SimulationRepository>()
        batchSimulationRepository = mockk<BatchSimulationRepository>()
        simulationService = SimulationService(feeService, simulationRepository, batchSimulationRepository)
    }

    @Test
    fun `should calculate loan simulation correctly for senior client`() {
        val simulation = createLoanSimulation(BigDecimal("10000.00"), 65, 12)
        every { feeService.calculateFeeRate(any()) } returns 0.04

        val result = simulationService.simulate(simulation)

        verify { feeService.calculateFeeRate(simulation.birthdate) }
        assertEquals(BigDecimal("851.50"), result.monthlyInstallmentAmount)
        assertEquals(BigDecimal("10218.00"), result.totalAmountToBePaid)
        assertEquals(BigDecimal("218.00"), result.totalFeePaid)
    }

    @Test
    fun `should calculate loan simulation correctly for middle-aged client`() {
        val simulation = createLoanSimulation(BigDecimal("5000.00"), 45, 24)
        every { feeService.calculateFeeRate(any()) } returns 0.02

        val result = simulationService.simulate(simulation)

        verify { feeService.calculateFeeRate(simulation.birthdate) }
        assertEquals(BigDecimal("212.70"), result.monthlyInstallmentAmount)
        assertEquals(BigDecimal("5104.80"), result.totalAmountToBePaid)
        assertEquals(BigDecimal("104.80"), result.totalFeePaid)
    }

    @Test
    fun `should calculate loan simulation correctly for adult client`() {
        val simulation = createLoanSimulation(BigDecimal("15000.00"), 30, 36)
        every { feeService.calculateFeeRate(any()) } returns 0.03

        val result = simulationService.simulate(simulation)

        verify { feeService.calculateFeeRate(simulation.birthdate) }
        assertEquals(BigDecimal("436.22"), result.monthlyInstallmentAmount)
        assertEquals(BigDecimal("15703.92"), result.totalAmountToBePaid)
        assertEquals(BigDecimal("703.92"), result.totalFeePaid)
    }

    @Test
    fun `should calculate loan simulation correctly for young client`() {
        val simulation = createLoanSimulation(BigDecimal("80000.00"), 22, 60)
        every { feeService.calculateFeeRate(any()) } returns 0.05

        val result = simulationService.simulate(simulation)

        verify { feeService.calculateFeeRate(simulation.birthdate) }

        assertEquals(BigDecimal("1509.70"), result.monthlyInstallmentAmount)
        assertEquals(BigDecimal("90582.00"), result.totalAmountToBePaid)
        assertEquals(BigDecimal("10582.00"), result.totalFeePaid)
    }

    @Test
    fun `should call fee service with correct birthdate`() {
        val specificDateString = createDateStringFromAge(28)
        val simulation = LoanApplication(
            amount = BigDecimal("7500.00"),
            birthdate = specificDateString,
            installments = 20
        )
        every { feeService.calculateFeeRate(specificDateString) } returns 3.2

        simulationService.simulate(simulation)

        verify(exactly = 1) { feeService.calculateFeeRate(specificDateString) }
    }

    @Test
    fun `should save successful simulation correctly`() {
        val batchId = UUID.randomUUID()
        val loanApplication = LoanApplication(
            amount = BigDecimal("10000.00"),
            birthdate = "15/03/1990",
            installments = 12
        )
        val loanSimulation = LoanSimulation(
            monthlyInstallmentAmount = BigDecimal("900.00"),
            totalAmountToBePaid = BigDecimal("10800.00"),
            totalFeePaid = BigDecimal("800.00")
        )

        val savedEntity = SimulationEntity(
            id = UUID.randomUUID(),
            batchId = batchId,
            status = SimulationStatus.COMPLETED,
            amountRequested = loanApplication.amount,
            birthdate = LocalDate.parse(loanApplication.birthdate, DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            installments = loanApplication.installments,
            totalAmount = loanSimulation.totalAmountToBePaid,
            installmentAmount = loanSimulation.monthlyInstallmentAmount,
            totalFee = loanSimulation.totalFeePaid,
            processedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { simulationRepository.save(any<SimulationEntity>()) } returns savedEntity

        val result = simulationService.saveSuccessfulSimulation(batchId, loanApplication, loanSimulation)

        verify { simulationRepository.save(any<SimulationEntity>()) }
        assertEquals(SimulationStatus.COMPLETED, result.status)
        assertEquals(loanApplication.amount, result.amountRequested)
        assertEquals(
            LocalDate.parse(loanApplication.birthdate, DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            result.birthdate
        )
        assertEquals(loanApplication.installments, result.installments)
        assertEquals(loanSimulation.totalAmountToBePaid, result.totalAmount)
        assertEquals(loanSimulation.monthlyInstallmentAmount, result.installmentAmount)
        assertEquals(loanSimulation.totalFeePaid, result.totalFee)
        assertNotNull(result.processedAt)
        assertNotNull(result.createdAt)
    }

    @Test
    fun `should save failed simulation correctly`() {
        val batchId = UUID.randomUUID()
        val loanApplication = LoanApplication(
            amount = BigDecimal("10000.00"),
            birthdate = "15/03/1990",
            installments = 12
        )
        val error = RuntimeException("Simulation failed")

        val savedEntity = SimulationEntity(
            id = UUID.randomUUID(),
            batchId = batchId,
            status = SimulationStatus.FAILED,
            amountRequested = loanApplication.amount,
            birthdate = LocalDate.parse(loanApplication.birthdate, DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            installments = loanApplication.installments,
            processedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { simulationRepository.save(any<SimulationEntity>()) } returns savedEntity

        val result = simulationService.saveFailedSimulation(batchId, loanApplication, error)

        verify { simulationRepository.save(any<SimulationEntity>()) }

        assertEquals(SimulationStatus.FAILED, result.status)
        assertEquals(loanApplication.amount, result.amountRequested)
        assertEquals(
            LocalDate.parse(loanApplication.birthdate, DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            result.birthdate
        )
        assertEquals(loanApplication.installments, result.installments)
        assertNotNull(result.processedAt)
        assertNotNull(result.createdAt)
    }

    @Test
    fun `should throw exception when batch not found for successful simulation`() {
        val batchId = UUID.randomUUID()
        val loanApplication = LoanApplication(
            amount = BigDecimal("10000.00"),
            birthdate = "15/03/1990",
            installments = 12
        )
        val loanSimulation = LoanSimulation(
            monthlyInstallmentAmount = BigDecimal("900.00"),
            totalAmountToBePaid = BigDecimal("10800.00"),
            totalFeePaid = BigDecimal("800.00")
        )

        every { batchSimulationRepository.findById(batchId) } returns Optional.empty()

        assertThrows<Exception> {
            simulationService.saveSuccessfulSimulation(batchId, loanApplication, loanSimulation)
        }
    }

    @Test
    fun `should throw exception when batch not found for failed simulation`() {
        val batchId = UUID.randomUUID()
        val loanApplication = LoanApplication(
            amount = BigDecimal("10000.00"),
            birthdate = "15/03/1990",
            installments = 12
        )
        val error = RuntimeException("Simulation failed")

        every { batchSimulationRepository.findById(batchId) } returns Optional.empty()

        assertThrows<Exception> {
            simulationService.saveFailedSimulation(batchId, loanApplication, error)
        }
    }

    private fun createLoanSimulation(amount: BigDecimal, age: Int, installments: Int): LoanApplication {
        val birthDateString = createDateStringFromAge(age)
        return LoanApplication(
            amount = amount,
            birthdate = birthDateString,
            installments = installments
        )
    }

    private fun createDateStringFromAge(age: Int): String {
        val birthLocalDate = LocalDate.now().minusYears(age.toLong())
        return birthLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}
