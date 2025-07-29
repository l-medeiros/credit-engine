package com.lucasmedeiros.creditengine.controller

import com.lucasmedeiros.creditengine.controller.request.LoanSimulationRequest
import com.lucasmedeiros.creditengine.controller.response.LoanSimulationResponse
import com.lucasmedeiros.creditengine.service.SimulationService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/simulations")
@Tag(name = "Loan Simulations", description = "Loan Simulations RESTful API")
class SimulationsController(private val simulationService: SimulationService) {

    private val logger = LoggerFactory.getLogger(SimulationsController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun simulate(@Valid @RequestBody loanRequest: LoanSimulationRequest): LoanSimulationResponse {
        logger.info("Starting loan simulation for $loanRequest")

        return try {
            val loanSimulation = loanRequest.toDomain()
            val result = simulationService.simulate(loanSimulation)

            LoanSimulationResponse.fromDomain(result).also {
                logger.info("Loan simulation completed successfully for $loanRequest result=$it")
            }
        } catch (exception: Exception) {
            logger.error("Error processing loan simulation for $loanRequest error: ${exception.message}")
            throw exception
        }
    }
}
