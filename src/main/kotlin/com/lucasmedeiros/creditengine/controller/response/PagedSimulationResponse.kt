package com.lucasmedeiros.creditengine.controller.response

import org.springframework.data.domain.Page

data class PagedSimulationResponse(
    val content: List<SimulationResultResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
) {
    companion object {
        fun fromPage(page: Page<SimulationResultResponse>): PagedSimulationResponse {
            return PagedSimulationResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                first = page.isFirst,
                last = page.isLast
            )
        }
    }
}
