package com.lucasmedeiros.creditengine.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lucasmedeiros.creditengine.controller.request.LoanApplicationRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SimulationsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return 200 with simulation result when request is valid`() {
        val birthdate = LocalDate.now().minusYears(65).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val validRequest = LoanApplicationRequest(
            amount = BigDecimal("10000.00"),
            birthdate = birthdate,
            installments = 12
        )

        mockMvc.post("/simulations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalAmount") { value(10218.00) }
            jsonPath("$.installmentAmount") { value(851.50) }
            jsonPath("$.totalFee") { value(218.00) }
        }
    }

    @Test
    fun `should return 400 when amount is negative`() {
        val invalidRequest = LoanApplicationRequest(
            amount = BigDecimal("-1000.00"),
            birthdate = "15/03/1990",
            installments = 12
        )

        mockMvc.post("/simulations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("amount") }
            jsonPath("$.errors[0].message") { value("Amount must be positive") }
        }
    }

    @Test
    fun `should return 400 when birthdate format is invalid`() {
        val invalidRequest = LoanApplicationRequest(
            amount = BigDecimal("1000.00"),
            birthdate = "15/03-1990",
            installments = 12
        )

        mockMvc.post("/simulations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("birthdate") }
            jsonPath("$.errors[0].message") { value("Birthdate must be in dd/MM/yyyy format") }
        }
    }

    @Test
    fun `should return 400 when installments is zero`() {
        val invalidRequest = LoanApplicationRequest(
            amount = BigDecimal("1000.00"),
            birthdate = "15/03/1990",
            installments = 0
        )

        mockMvc.post("/simulations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("installments") }
            jsonPath("$.errors[0].message") { value("Number of installments must be positive") }
        }
    }

    @Test
    fun `should return 400 when amount is zero`() {
        val invalidRequest = LoanApplicationRequest(
            amount = BigDecimal.ZERO,
            birthdate = "15/03/1990",
            installments = 12
        )

        mockMvc.post("/simulations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("amount") }
            jsonPath("$.errors[0].message") { value("Amount must be positive") }
        }
    }

    @Test
    fun `should return 400 when installments is negative`() {
        val invalidRequest = LoanApplicationRequest(
            amount = BigDecimal("1000.00"),
            birthdate = "15/03/1990",
            installments = -5
        )

        mockMvc.post("/simulations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("installments") }
            jsonPath("$.errors[0].message") { value("Number of installments must be positive") }
        }
    }

    @Test
    fun `should return 400 when birthdate is empty`() {
        val invalidRequest = LoanApplicationRequest(
            amount = BigDecimal("1000.00"),
            birthdate = "",
            installments = 12
        )

        mockMvc.post("/simulations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("birthdate") }
        }
    }
}
