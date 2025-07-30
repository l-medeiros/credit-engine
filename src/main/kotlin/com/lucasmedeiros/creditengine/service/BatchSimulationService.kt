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
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
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

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    publishBatchSimulationCreatedEvent(batch, request)
                }
            }
        )

        return BatchSimulationResponse(
            batchId = batch.id!!,
            status = batch.status.name,
            createdAt = batch.createdAt!!
        )
    }

    @Transactional
    fun incrementCompletedSimulations(batchId: UUID) {
        val updatedRows = batchSimulationRepository.incrementCompletedSimulations(batchId)

        if (updatedRows == 0) {
            logger.error("No batch found with id: $batchId")
            throw IllegalArgumentException("Batch not found: $batchId")
        }

        checkAndMarkBatchAsCompleted(batchId)
    }

    @Transactional
    fun incrementFailedSimulations(batchId: UUID) {
        val updatedRows = batchSimulationRepository.incrementFailedSimulations(batchId)

        if (updatedRows == 0) {
            logger.error("No batch found with id: $batchId")
            throw IllegalArgumentException("Batch not found: $batchId")
        }

        checkAndMarkBatchAsCompleted(batchId)
    }

    private fun checkAndMarkBatchAsCompleted(batchId: UUID) {
        val batch = batchSimulationRepository.findById(batchId).orElse(null)
        if (batch != null) {
            val processedSimulations = batch.completedSimulations + batch.failedSimulations

            if (processedSimulations >= batch.totalSimulations) {
                batchSimulationRepository.markAsCompleted(batchId, LocalDateTime.now())
                logger.info(
                    "Batch completed: batchId=$batchId, completed=${batch.completedSimulations}, " +
                    "failed=${batch.failedSimulations}, total=${batch.totalSimulations}"
                )
            } else {
                logger.info("Batch in progress: batchId=$batchId")
            }
        }
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

    fun getBatchStatus(batchId: UUID): BatchSimulationEntity? {
        return batchSimulationRepository.findById(batchId).orElse(null)
    }
}
