package com.lucasmedeiros.creditengine.infra.event.handler

import com.lucasmedeiros.creditengine.domain.event.SimulationProcessingEvent
import com.lucasmedeiros.creditengine.service.BatchSimulationService
import com.lucasmedeiros.creditengine.service.SimulationService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class SimulationProcessingEventHandler(
    private val simulationService: SimulationService,
    private val batchSimulationService: BatchSimulationService
) {

    private val logger = LoggerFactory.getLogger(SimulationProcessingEventHandler::class.java)

    @Async
    @EventListener
    fun handle(event: SimulationProcessingEvent) {
        logger.info("Processing simulation for batchId=${event.batchId}, eventId=${event.id}")

        try {
            val result = simulationService.simulate(event.loanApplication)

            simulationService.saveSuccessfulSimulation(
                batchId = event.batchId,
                loanApplication = event.loanApplication,
                result = result
            )

            batchSimulationService.incrementCompletedSimulations(event.batchId)

            logger.info("Simulation completed successfully for batchId=${event.batchId}, eventId=${event.id}")
        } catch (exception: Exception) {
            logger.error(
                "Error processing simulation for batchId=${event.batchId}, eventId=${event.id}: ${exception.message}",
                exception
            )

            simulationService.saveFailedSimulation(
                batchId = event.batchId,
                loanApplication = event.loanApplication,
                error = exception
            )

            batchSimulationService.incrementFailedSimulations(event.batchId)
        }
    }
}
