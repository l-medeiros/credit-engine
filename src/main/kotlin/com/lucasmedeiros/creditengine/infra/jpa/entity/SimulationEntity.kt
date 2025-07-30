package com.lucasmedeiros.creditengine.infra.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.Column
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "simulation")
data class SimulationEntity(
    @Id
    val id: UUID? = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: SimulationStatus = SimulationStatus.PENDING,

    @Column(name = "amount_requested", nullable = false, precision = 15, scale = 2)
    val amountRequested: BigDecimal,

    @Column(nullable = true)
    val birthdate: LocalDate?,

    @Column(nullable = false)
    val installments: Int,

    @Column(name = "total_amount", precision = 15, scale = 2)
    val totalAmount: BigDecimal? = null,

    @Column(name = "installment_amount", precision = 15, scale = 2)
    val installmentAmount: BigDecimal? = null,

    @Column(name = "total_fee", precision = 15, scale = 2)
    val totalFee: BigDecimal? = null,

    @Column(name = "processed_at")
    val processedAt: LocalDateTime? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "batch_id")
    val batchId: UUID? = null
)

enum class SimulationStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
