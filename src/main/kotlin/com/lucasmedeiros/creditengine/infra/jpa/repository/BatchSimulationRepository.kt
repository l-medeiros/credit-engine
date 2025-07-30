package com.lucasmedeiros.creditengine.infra.jpa.repository

import com.lucasmedeiros.creditengine.infra.jpa.entity.BatchSimulationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BatchSimulationRepository : JpaRepository<BatchSimulationEntity, UUID>
