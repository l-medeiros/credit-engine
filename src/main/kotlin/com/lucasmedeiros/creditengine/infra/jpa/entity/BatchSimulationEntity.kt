package com.lucasmedeiros.creditengine.infra.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.Column
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "batch_simulation")
data class BatchSimulationEntity(
    @Id
    val id: UUID? = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: BatchStatus = BatchStatus.PENDING,

    @Column(name = "total_simulations", nullable = false)
    val totalSimulations: Int = 0,

    @Column(name = "completed_simulations")
    val completedSimulations: Int = 0,

    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    val completedAt: LocalDateTime? = null
)

enum class BatchStatus {
    PENDING,
    PROCESSING,
    COMPLETED
}
