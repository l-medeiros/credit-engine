package com.lucasmedeiros.creditengine.domain.event

import com.lucasmedeiros.creditengine.domain.LoanApplication
import java.util.UUID

data class BatchSimulationCreatedEvent(
    val batchId: UUID,
    val loanApplications: List<LoanApplication>
) : Event(eventType = "BatchSimulationCreated")
