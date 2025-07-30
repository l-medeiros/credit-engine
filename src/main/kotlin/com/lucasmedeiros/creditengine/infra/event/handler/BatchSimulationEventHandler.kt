package com.lucasmedeiros.creditengine.infra.event.handler

import com.lucasmedeiros.creditengine.domain.event.BatchSimulationCreatedEvent
import com.lucasmedeiros.creditengine.domain.event.SimulationProcessingEvent
import com.lucasmedeiros.creditengine.domain.event.EventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BatchSimulationEventHandler(
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(BatchSimulationEventHandler::class.java)

    @EventListener
    fun handle(event: BatchSimulationCreatedEvent) {
        logger.info("Processing batch simulation: batchId=${event.batchId}, eventId=${event.id}")

        event.loanApplications.forEach { loanApplication ->
            val simulationEvent = SimulationProcessingEvent(
                batchId = event.batchId,
                loanApplication = loanApplication
            )

            eventPublisher.publish(simulationEvent)
            logger.debug("Published simulation event for batchId=${event.batchId}, eventId=${simulationEvent.id}")
        }

        logger.info("Published ${event.loanApplications.size} simulation events for batchId=${event.batchId}")
    }
}
