package com.lucasmedeiros.creditengine.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lucasmedeiros.creditengine.controller.request.BatchLoanApplicationRequest
import com.lucasmedeiros.creditengine.controller.response.BatchSimulationResponse
import com.lucasmedeiros.creditengine.domain.LoanApplication
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

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
        val validRequest = LoanApplication(
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
        val invalidRequest = LoanApplication(
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
        val invalidRequest = LoanApplication(
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
        val invalidRequest = LoanApplication(
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
        val invalidRequest = LoanApplication(
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
        val invalidRequest = LoanApplication(
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
        val invalidRequest = LoanApplication(
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

    @Test
    fun `should return 202 when batch simulation request is valid`() {
        val loanApplications = listOf(
            LoanApplication(
                amount = BigDecimal("10000.00"),
                birthdate = "15/03/1990",
                installments = 12
            ),
            LoanApplication(
                amount = BigDecimal("5000.00"),
                birthdate = "20/05/1985",
                installments = 6
            )
        )
        val validRequest = BatchLoanApplicationRequest(loanApplications = loanApplications)

        mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(validRequest)
        }.andExpect {
            status { isAccepted() }
            jsonPath("$.batchId") { exists() }
            jsonPath("$.status") { value("PENDING") }
            jsonPath("$.createdAt") { exists() }
        }
    }

    @Test
    fun `should return 400 when batch simulation request is empty`() {
        val invalidRequest = BatchLoanApplicationRequest(loanApplications = emptyList())

        mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("loanApplications") }
            jsonPath("$.errors[0].message") { value("Batch size must be between 1 and 10000") }
        }
    }

    @Test
    fun `should return 400 when batch simulation exceeds maximum size`() {
        val birthdate = LocalDate.now().minusYears(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val loanApplications = (1..10001).map {
            LoanApplication(
                amount = BigDecimal("1000.00"),
                birthdate = birthdate,
                installments = 12
            )
        }
        val invalidRequest = BatchLoanApplicationRequest(loanApplications = loanApplications)

        mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("loanApplications") }
            jsonPath("$.errors[0].message") { value("Batch size must be between 1 and 10000") }
        }
    }

    @Test
    fun `should return batch status when batch exists`() {
        val loanApplications = listOf(
            LoanApplication(
                amount = BigDecimal("10000.00"),
                birthdate = "15/03/1990",
                installments = 12
            )
        )
        val batchRequest = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val createResult = mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(batchRequest)
        }.andExpect {
            status { isAccepted() }
        }.andReturn()

        val batchResponse = objectMapper.readValue(
            createResult.response.contentAsString,
            BatchSimulationResponse::class.java
        )

        mockMvc.get("/simulations/batch/${batchResponse.batchId}") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.batchId") { value(batchResponse.batchId.toString()) }
            jsonPath("$.status") { exists() }
            jsonPath("$.totalSimulations") { value(1) }
            jsonPath("$.completedSimulations") { exists() }
            jsonPath("$.failedSimulations") { exists() }
            jsonPath("$.createdAt") { exists() }
        }
    }

    @Test
    fun `should return 404 when batch does not exist`() {
        val nonExistentBatchId = UUID.randomUUID()

        mockMvc.get("/simulations/batch/$nonExistentBatchId") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should process batch simulations asynchronously and update status`() {
        val loanApplications = listOf(
            LoanApplication(
                amount = BigDecimal("10000.00"),
                birthdate = "15/03/1990",
                installments = 12
            ),
            LoanApplication(
                amount = BigDecimal("5000.00"),
                birthdate = "20/05/1985",
                installments = 6
            )
        )
        val batchRequest = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val createResult = mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(batchRequest)
        }.andExpect {
            status { isAccepted() }
            jsonPath("$.status") { value("PENDING") }
        }.andReturn()

        val batchResponse = objectMapper.readValue(
            createResult.response.contentAsString,
            BatchSimulationResponse::class.java
        )

        Thread.sleep(2000)

        mockMvc.get("/simulations/batch/${batchResponse.batchId}") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.batchId") { value(batchResponse.batchId.toString()) }
            jsonPath("$.totalSimulations") { value(2) }
            jsonPath("$.completedSimulations") { value(greaterThanOrEqualTo(0)) }
            jsonPath("$.failedSimulations") { value(greaterThanOrEqualTo(0)) }
        }

        Thread.sleep(3000)
        mockMvc.get("/simulations/batch/${batchResponse.batchId}") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("COMPLETED") }
            jsonPath("$.completedAt") { exists() }
            jsonPath("$.completedSimulations") { exists() }
            jsonPath("$.failedSimulations") { exists() }
        }
    }

    @Test
    fun `should return paginated successful simulations for specific batch with default pagination`() {
        val loanApplications = listOf(
            LoanApplication(
                amount = BigDecimal("10000.00"),
                birthdate = "15/03/1990",
                installments = 12
            ),
            LoanApplication(
                amount = BigDecimal("5000.00"),
                birthdate = "20/05/1985",
                installments = 6
            )
        )
        val batchRequest = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val createResult = mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(batchRequest)
        }.andExpect {
            status { isAccepted() }
        }.andReturn()

        val batchResponse = objectMapper.readValue(
            createResult.response.contentAsString,
            BatchSimulationResponse::class.java
        )

        Thread.sleep(3000)

        mockMvc.get("/simulations/batch/${batchResponse.batchId}/results") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.page") { value(0) }
            jsonPath("$.size") { value(20) }
            jsonPath("$.totalElements") { exists() }
            jsonPath("$.totalPages") { exists() }
            jsonPath("$.first") { value(true) }
            jsonPath("$.last") { exists() }
        }
    }

    @Test
    fun `should return paginated successful simulations for specific batch with custom pagination`() {
        val loanApplications = listOf(
            LoanApplication(
                amount = BigDecimal("10000.00"),
                birthdate = "15/03/1990",
                installments = 12
            )
        )
        val batchRequest = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val createResult = mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(batchRequest)
        }.andExpect {
            status { isAccepted() }
        }.andReturn()

        val batchResponse = objectMapper.readValue(
            createResult.response.contentAsString,
            BatchSimulationResponse::class.java
        )

        Thread.sleep(3000)

        mockMvc.get("/simulations/batch/${batchResponse.batchId}/results") {
            contentType = MediaType.APPLICATION_JSON
            param("page", "0")
            param("size", "5")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.page") { value(0) }
            jsonPath("$.size") { value(5) }
            jsonPath("$.totalElements") { exists() }
            jsonPath("$.totalPages") { exists() }
        }
    }

    @Test
    fun `should return 404 when requesting results for non-existent batch`() {
        val nonExistentBatchId = UUID.randomUUID()

        mockMvc.get("/simulations/batch/$nonExistentBatchId/results") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `should return empty page when batch has no successful simulations`() {
        val loanApplications = listOf(
            LoanApplication(
                amount = BigDecimal("10000.00"),
                birthdate = "15/03/1990",
                installments = 12
            )
        )
        val batchRequest = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val createResult = mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(batchRequest)
        }.andExpect {
            status { isAccepted() }
        }.andReturn()

        val batchResponse = objectMapper.readValue(
            createResult.response.contentAsString,
            BatchSimulationResponse::class.java
        )

        mockMvc.get("/simulations/batch/${batchResponse.batchId}/results") {
            contentType = MediaType.APPLICATION_JSON
            param("page", "999")
            param("size", "10")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isEmpty() }
            jsonPath("$.page") { value(999) }
            jsonPath("$.size") { value(10) }
            jsonPath("$.totalElements") { exists() }
            jsonPath("$.totalPages") { exists() }
        }
    }

    @Test
    fun `should validate successful simulations response structure for specific batch`() {
        val loanApplications = listOf(
            LoanApplication(
                amount = BigDecimal("10000.00"),
                birthdate = "15/03/1990",
                installments = 12
            )
        )
        val batchRequest = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val createResult = mockMvc.post("/simulations/batch") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(batchRequest)
        }.andExpect {
            status { isAccepted() }
        }.andReturn()

        val batchResponse = objectMapper.readValue(
            createResult.response.contentAsString,
            BatchSimulationResponse::class.java
        )

        Thread.sleep(3000)

        mockMvc.get("/simulations/batch/${batchResponse.batchId}/results") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[*].id") { exists() }
            jsonPath("$.content[*].amountRequested") { exists() }
            jsonPath("$.content[*].installments") { exists() }
            jsonPath("$.content[*].totalAmount") { exists() }
            jsonPath("$.content[*].installmentAmount") { exists() }
            jsonPath("$.content[*].totalFee") { exists() }
            jsonPath("$.content[*].processedAt") { exists() }
            jsonPath("$.content[*].createdAt") { exists() }
        }
    }
}
