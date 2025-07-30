package com.lucasmedeiros.creditengine.infra.jpa.repository

import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationEntity
import com.lucasmedeiros.creditengine.infra.jpa.entity.SimulationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SimulationRepository : JpaRepository<SimulationEntity, UUID> {

    @Query(
        "SELECT s FROM SimulationEntity s WHERE s.batchId = :batchId AND s.status = :status ORDER BY s.processedAt DESC"
    )
    fun findByBatchIdAndStatusOrderByProcessedAtDesc(
        batchId: UUID,
        status: SimulationStatus,
        pageable: Pageable
    ): Page<SimulationEntity>
}
