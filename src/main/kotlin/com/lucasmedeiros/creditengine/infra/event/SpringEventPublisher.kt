package com.lucasmedeiros.creditengine.infra.event

import com.lucasmedeiros.creditengine.domain.event.Event
import com.lucasmedeiros.creditengine.domain.event.EventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : EventPublisher {

    override fun publish(event: Event) {
        applicationEventPublisher.publishEvent(event)
    }

    override fun publishAll(events: List<Event>) {
        events.forEach { publish(it) }
    }
}
