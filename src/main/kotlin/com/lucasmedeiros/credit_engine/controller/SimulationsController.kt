package com.lucasmedeiros.credit_engine.controller

import com.lucasmedeiros.credit_engine.controller.request.LoanSimulationRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/simulations")
class SimulationsController {

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun simulate(@Valid @RequestBody loanRequest: LoanSimulationRequest): Unit {
        return Unit
    }
}