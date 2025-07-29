package com.lucasmedeiros.creditengine.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date

@Service
class FeeService {

    private val logger = LoggerFactory.getLogger(FeeService::class.java)

    companion object {
        private const val SENIOR_AGE = 60
        private const val MIDDLE_AGE = 40
        private const val YOUNG_ADULT_AGE = 25

        private const val SENIOR_FEE_RATE = 0.04
        private const val MIDDLE_AGE_FEE_RATE = 0.02
        private const val YOUNG_ADULT_FEE_RATE = 0.03
        private const val YOUTH_FEE_RATE = 0.05
    }

    fun calculateFeeRate(leadBirthdate: Date): Double {
        val leadAge = calculateAge(leadBirthdate)
        logger.info("Calculating fees for age: $leadAge")

        return when {
            leadAge > SENIOR_AGE -> SENIOR_FEE_RATE
            leadAge > MIDDLE_AGE -> MIDDLE_AGE_FEE_RATE
            leadAge > YOUNG_ADULT_AGE -> YOUNG_ADULT_FEE_RATE
            else -> YOUTH_FEE_RATE
        }.also { logger.info("Calculated fee rate: ${it * 100}% for age $leadAge") }
    }

    private fun calculateAge(birthDate: Date): Int {
        val birth = birthDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()

        return Period.between(birth, today).years
    }
}
