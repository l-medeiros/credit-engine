package com.lucasmedeiros.creditengine.infra.jpa.repository

import com.lucasmedeiros.creditengine.infra.jpa.entity.BatchSimulationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface BatchSimulationRepository : JpaRepository<BatchSimulationEntity, UUID> {

    @Modifying
    @Query("""
        UPDATE BatchSimulationEntity b 
        SET b.completedSimulations = b.completedSimulations + 1 
        WHERE b.id = :batchId
    """)
    fun incrementCompletedSimulations(@Param("batchId") batchId: UUID): Int

    @Modifying
    @Query("""
        UPDATE BatchSimulationEntity b 
        SET b.failedSimulations = b.failedSimulations + 1 
        WHERE b.id = :batchId
    """)
    fun incrementFailedSimulations(@Param("batchId") batchId: UUID): Int

    @Modifying
    @Query("""
        UPDATE BatchSimulationEntity b 
        SET b.status = 'COMPLETED', b.completedAt = :completedAt 
        WHERE b.id = :batchId
    """)
    fun markAsCompleted(
        @Param("batchId") batchId: UUID,
        @Param("completedAt") completedAt: LocalDateTime = LocalDateTime.now()
    ): Int
}
