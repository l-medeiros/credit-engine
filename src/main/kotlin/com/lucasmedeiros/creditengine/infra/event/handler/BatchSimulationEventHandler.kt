package com.lucasmedeiros.creditengine.infra.event.handler

import com.lucasmedeiros.creditengine.domain.event.BatchSimulationCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BatchSimulationEventHandler {

    private val logger = LoggerFactory.getLogger(BatchSimulationEventHandler::class.java)

    @EventListener
    fun handle(event: BatchSimulationCreatedEvent) {
        logger.info("Batch simulation created: batchId=${event.batchId}, eventId=${event.id}")
    }
}
