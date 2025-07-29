package com.lucasmedeiros.creditengine.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lucasmedeiros.creditengine.controller.request.LoanSimulationRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@WebMvcTest(SimulationsController::class)
class SimulationsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should return 200 when request is valid`() {
        val validRequest = LoanSimulationRequest(
            amount = BigDecimal("1000.00"),
            birthdate = "15/03/1990",
            installments = 12
        )

        mockMvc.post("/simulations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validRequest)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should return 400 when amount is negative`() {
        val invalidRequest = LoanSimulationRequest(
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
        val invalidRequest = LoanSimulationRequest(
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
        val invalidRequest = LoanSimulationRequest(
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
        val invalidRequest = LoanSimulationRequest(
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
        val invalidRequest = LoanSimulationRequest(
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
        val invalidRequest = LoanSimulationRequest(
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
