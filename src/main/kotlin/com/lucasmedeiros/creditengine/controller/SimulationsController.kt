package com.lucasmedeiros.creditengine.controller

import com.lucasmedeiros.creditengine.controller.request.LoanSimulationRequest
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/simulations")
@Tag(name = "Loan Simulations", description = "Loan Simulations RESTful API")
class SimulationsController {

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun simulate(@Valid @RequestBody loanRequest: LoanSimulationRequest) {
        return Unit
    }
}
