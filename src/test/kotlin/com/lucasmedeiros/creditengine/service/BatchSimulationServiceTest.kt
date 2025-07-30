package com.lucasmedeiros.creditengine.service

import com.lucasmedeiros.creditengine.controller.request.BatchLoanApplicationRequest
import com.lucasmedeiros.creditengine.domain.LoanApplication
import com.lucasmedeiros.creditengine.domain.event.BatchSimulationCreatedEvent
import com.lucasmedeiros.creditengine.domain.event.EventPublisher
import com.lucasmedeiros.creditengine.infra.jpa.entity.BatchSimulationEntity
import com.lucasmedeiros.creditengine.infra.jpa.entity.BatchStatus
import com.lucasmedeiros.creditengine.infra.jpa.repository.BatchSimulationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class BatchSimulationServiceTest {

    private val batchSimulationRepository = mockk<BatchSimulationRepository>()
    private val eventPublisher = mockk<EventPublisher>(relaxed = true)
    private val batchSimulationService = BatchSimulationService(batchSimulationRepository, eventPublisher)

    @Test
    fun `should create batch simulation successfully`() {
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
        val request = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val savedEntity = BatchSimulationEntity(
            id = UUID.randomUUID(),
            status = BatchStatus.PENDING,
            totalSimulations = 2,
            createdAt = LocalDateTime.now()
        )

        every { batchSimulationRepository.save(any()) } returns savedEntity

        val result = batchSimulationService.createBatchSimulation(request)

        assertEquals(savedEntity.id, result.batchId)
        assertEquals("PENDING", result.status)
        assertEquals(savedEntity.createdAt, result.createdAt)

        verify { batchSimulationRepository.save(any()) }
        verify { eventPublisher.publish(any<BatchSimulationCreatedEvent>()) }
    }

    @Test
    fun `should publish event with correct data`() {
        val loanApplications = listOf(
            LoanApplication(
                amount = BigDecimal("15000.00"),
                birthdate = "10/12/1988",
                installments = 24
            )
        )
        val request = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val savedEntity = BatchSimulationEntity(
            id = UUID.randomUUID(),
            status = BatchStatus.PENDING,
            totalSimulations = 1,
            createdAt = LocalDateTime.now()
        )

        every { batchSimulationRepository.save(any()) } returns savedEntity

        batchSimulationService.createBatchSimulation(request)

        verify {
            eventPublisher.publish(
                match<BatchSimulationCreatedEvent> { event ->
                    event.batchId == savedEntity.id &&
                    event.loanApplications == loanApplications &&
                    event.eventType == "BatchSimulationCreated"
                }
            )
        }
    }

    @Test
    fun `should handle empty list correctly`() {
        val request = BatchLoanApplicationRequest(loanApplications = emptyList())

        val savedEntity = BatchSimulationEntity(
            id = UUID.randomUUID(),
            status = BatchStatus.PENDING,
            totalSimulations = 0,
            createdAt = LocalDateTime.now()
        )

        every { batchSimulationRepository.save(any()) } returns savedEntity

        val result = batchSimulationService.createBatchSimulation(request)

        assertEquals(savedEntity.id, result.batchId)
        assertEquals("PENDING", result.status)

        verify { batchSimulationRepository.save(any()) }
        verify { eventPublisher.publish(any<BatchSimulationCreatedEvent>()) }
    }

    @Test
    fun `should handle large batch correctly`() {
        val loanApplications = (1..1000).map {
            LoanApplication(
                amount = BigDecimal("1000.00"),
                birthdate = "01/01/1990",
                installments = 12
            )
        }
        val request = BatchLoanApplicationRequest(loanApplications = loanApplications)

        val savedEntity = BatchSimulationEntity(
            id = UUID.randomUUID(),
            status = BatchStatus.PENDING,
            totalSimulations = 1000,
            createdAt = LocalDateTime.now()
        )

        every { batchSimulationRepository.save(any()) } returns savedEntity

        val result = batchSimulationService.createBatchSimulation(request)

        assertEquals(savedEntity.id, result.batchId)
        assertEquals("PENDING", result.status)

        verify { batchSimulationRepository.save(any()) }
        verify { eventPublisher.publish(any<BatchSimulationCreatedEvent>()) }
    }
}
