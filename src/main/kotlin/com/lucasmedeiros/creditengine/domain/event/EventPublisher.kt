package com.lucasmedeiros.creditengine.domain.event

interface EventPublisher {
    fun publish(event: Event)
    fun publishAll(events: List<Event>)
}
