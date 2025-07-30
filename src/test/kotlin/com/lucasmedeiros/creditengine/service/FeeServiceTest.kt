package com.lucasmedeiros.creditengine.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FeeServiceTest {

    private lateinit var feeService: FeeService

    @BeforeEach
    fun setUp() {
        feeService = FeeService()
    }

    @ParameterizedTest
    @CsvSource(
        "70, 0.04",
        "61, 0.04",
        "60, 0.02",
        "50, 0.02",
        "41, 0.02",
        "40, 0.03",
        "33, 0.03",
        "26, 0.03",
        "20, 0.05",
        "25, 0.05",
    )
    fun `should calculate correct fee rate for different ages`(age: Int, expectedRate: Double) {
        val birthDateString = createDateStringFromAge(age)

        val feeRate = feeService.calculateFeeRate(birthDateString)

        assertEquals(expectedRate, feeRate)
    }

    @Test
    fun `should handle current date calculation correctly`() {
        val thirtyYearsAgo = LocalDate.now().minusYears(30)
        val birthDateString = thirtyYearsAgo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        val feeRate = feeService.calculateFeeRate(birthDateString)

        assertEquals(0.03, feeRate)
    }

    private fun createDateStringFromAge(age: Int): String {
        val birthLocalDate = LocalDate.now().minusYears(age.toLong())
        return birthLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}
