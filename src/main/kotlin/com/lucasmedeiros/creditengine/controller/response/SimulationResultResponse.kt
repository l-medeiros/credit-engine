package com.lucasmedeiros.creditengine.controller.response

import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class SimulationResultResponse(
    val id: UUID,
    val batchId: UUID?,
    val status: String,
    val amountRequested: BigDecimal,
    val birthdate: LocalDate?,
    val installments: Int,
    val totalAmount: BigDecimal?,
    val installmentAmount: BigDecimal?,
    val totalFee: BigDecimal?,
    val processedAt: LocalDateTime?,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(entity: SimulationEntity): SimulationResultResponse {
            return SimulationResultResponse(
                id = entity.id!!,
                batchId = entity.batchId,
                status = entity.status.name,
                amountRequested = entity.amountRequested,
                birthdate = entity.birthdate,
                installments = entity.installments,
                totalAmount = entity.totalAmount,
                installmentAmount = entity.installmentAmount,
                totalFee = entity.totalFee,
                processedAt = entity.processedAt,
                createdAt = entity.createdAt
            )
        }
    }
}
