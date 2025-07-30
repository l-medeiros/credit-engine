package com.lucasmedeiros.creditengine.controller.response

import java.time.LocalDateTime
import java.util.UUID

data class BatchSimulationResponse(
    val batchId: UUID,
    val status: String,
    val createdAt: LocalDateTime
)
