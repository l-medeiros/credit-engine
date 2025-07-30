package com.lucasmedeiros.creditengine.domain.event

import com.lucasmedeiros.creditengine.domain.LoanApplication
import java.util.UUID

data class SimulationProcessingEvent(
    val batchId: UUID,
    val loanApplication: LoanApplication,
) : Event(eventType = "SimulationProcessingEvent")
