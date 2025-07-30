package com.lucasmedeiros.creditengine.service

import com.lucasmedeiros.creditengine.controller.request.BatchLoanApplicationRequest
import com.lucasmedeiros.creditengine.controller.response.BatchSimulationResponse
import com.lucasmedeiros.creditengine.domain.event.BatchSimulationCreatedEvent
import com.lucasmedeiros.creditengine.domain.event.EventPublisher
import com.lucasmedeiros.creditengine.infra.jpa.entity.BatchSimulationEntity
import com.lucasmedeiros.creditengine.infra.jpa.entity.BatchStatus
import com.lucasmedeiros.creditengine.infra.jpa.repository.BatchSimulationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class BatchSimulationService(
    private val batchSimulationRepository: BatchSimulationRepository,
    private val eventPublisher: EventPublisher
) {

    private val logger = LoggerFactory.getLogger(BatchSimulationService::class.java)

    @Transactional
    fun createBatchSimulation(request: BatchLoanApplicationRequest): BatchSimulationResponse {
        logger.info("Creating batch simulation")
        val batch = saveBatch(request)
        publishBatchSimulationCreatedEvent(batch, request)

        return BatchSimulationResponse(
            batchId = batch.id!!,
            status = batch.status.name,
            createdAt = batch.createdAt!!
        )
    }

    private fun saveBatch(request: BatchLoanApplicationRequest) = batchSimulationRepository.save(
        BatchSimulationEntity(
            id = UUID.randomUUID(),
            status = BatchStatus.PENDING,
            totalSimulations = request.loanApplications.size,
            createdAt = LocalDateTime.now()
        )
    ).also {
        logger.info("Batch simulation created with id: ${it.id}")
    }

    private fun publishBatchSimulationCreatedEvent(batch: BatchSimulationEntity, request: BatchLoanApplicationRequest) {
        val event = BatchSimulationCreatedEvent(
            batchId = batch.id!!,
            loanApplications = request.loanApplications
        )

        eventPublisher.publish(event)
        logger.info("BatchSimulationCreatedEvent published for batchId=${batch.id}")
    }
}
