package com.lucasmedeiros.creditengine.domain.event

import java.time.LocalDateTime
import java.util.UUID

abstract class Event(
    val id: UUID = UUID.randomUUID(),
    val publishedAt: LocalDateTime = LocalDateTime.now(),
    val eventType: String
)
