package com.lucasmedeiros.creditengine.controller

import com.lucasmedeiros.creditengine.controller.request.BatchLoanApplicationRequest
import com.lucasmedeiros.creditengine.controller.request.LoanApplicationRequest
import com.lucasmedeiros.creditengine.controller.response.BatchSimulationResponse
import com.lucasmedeiros.creditengine.controller.response.BatchStatusResponse
import com.lucasmedeiros.creditengine.controller.response.LoanSimulationResponse
import com.lucasmedeiros.creditengine.service.BatchSimulationService
import com.lucasmedeiros.creditengine.service.SimulationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@RestController
@RequestMapping("/simulations")
@Tag(name = "Loan Simulations", description = "Loan Simulations RESTful API")
class SimulationsController(
    private val simulationService: SimulationService,
    private val batchSimulationService: BatchSimulationService
) {

    private val logger = LoggerFactory.getLogger(SimulationsController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Simulate a single loan application")
    fun simulate(@Valid @RequestBody loanRequest: LoanApplicationRequest): LoanSimulationResponse =
        try {
            val loanSimulation = loanRequest.toDomain()
            val result = simulationService.simulate(loanSimulation)

            LoanSimulationResponse.fromDomain(result).also {
                logger.info("Loan simulation completed successfully for $loanRequest result=$it")
            }
        } catch (exception: Exception) {
            logger.error("Error processing loan simulation for $loanRequest error: ${exception.message}")
            throw exception
        }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Create a batch of loan simulations")
    fun simulateBatch(@Valid @RequestBody batchRequest: BatchLoanApplicationRequest): BatchSimulationResponse =
        try {
            batchSimulationService.createBatchSimulation(batchRequest).also {
                logger.info("Batch simulation created successfully: batchId=${it.batchId}")
            }
        } catch (exception: Exception) {
            logger.error("Error creating batch simulation: ${exception.message}")
            throw exception
        }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get batch simulation status")
    fun getBatchStatus(@PathVariable batchId: UUID): ResponseEntity<BatchStatusResponse> {
        return try {
            val batch = batchSimulationService.getBatchStatus(batchId)
            if (batch != null) {
                ResponseEntity.ok(BatchStatusResponse.fromEntity(batch)).also {
                    logger.info("Batch status retrieved successfully: batchId=$batchId")
                }
            } else {
                logger.warn("Batch not found: batchId=$batchId")
                ResponseEntity.notFound().build()
            }
        } catch (exception: Exception) {
            logger.error("Error retrieving batch status for batchId=$batchId: ${exception.message}")
            throw exception
        }
    }
}
