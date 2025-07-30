package com.lucasmedeiros.creditengine.controller.response

import com.lucasmedeiros.creditengine.infra.jpa.entity.BatchSimulationEntity
import java.time.LocalDateTime
import java.util.UUID

data class BatchStatusResponse(
    val batchId: UUID,
    val status: String,
    val totalSimulations: Int,
    val completedSimulations: Int,
    val failedSimulations: Int,
    val createdAt: LocalDateTime?,
    val completedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(entity: BatchSimulationEntity): BatchStatusResponse {
            return BatchStatusResponse(
                batchId = entity.id!!,
                status = entity.status.name,
                totalSimulations = entity.totalSimulations,
                completedSimulations = entity.completedSimulations,
                failedSimulations = entity.failedSimulations,
                createdAt = entity.createdAt,
                completedAt = entity.completedAt
            )
        }
    }
}
