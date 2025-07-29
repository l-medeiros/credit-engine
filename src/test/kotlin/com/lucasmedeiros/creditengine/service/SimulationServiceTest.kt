package com.lucasmedeiros.creditengine.service

import com.lucasmedeiros.creditengine.domain.LoanSimulation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class SimulationServiceTest {

    private lateinit var feeService: FeeService
    private lateinit var simulationService: SimulationService

    @BeforeEach
    fun setUp() {
        feeService = mockk<FeeService>()
        simulationService = SimulationService(feeService)
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
        val specificDate = createDateFromAge(28)
        val simulation = LoanSimulation(
            amount = BigDecimal("7500.00"),
            birthdate = specificDate,
            installments = 20
        )
        every { feeService.calculateFeeRate(specificDate) } returns 3.2

        simulationService.simulate(simulation)

        verify(exactly = 1) { feeService.calculateFeeRate(specificDate) }
    }

    private fun createLoanSimulation(amount: BigDecimal, age: Int, installments: Int): LoanSimulation {
        val birthDate = createDateFromAge(age)
        return LoanSimulation(
            amount = amount,
            birthdate = birthDate,
            installments = installments
        )
    }

    private fun createDateFromAge(age: Int): Date {
        val birthLocalDate = LocalDate.now().minusYears(age.toLong())
        return Date.from(birthLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}
