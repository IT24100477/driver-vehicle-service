package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareEstimateResponse;
import com.ridelink.farepayment.dto.FinalFareRequest;
import com.ridelink.farepayment.dto.FinalFareResponse;
import com.ridelink.farepayment.service.FareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/fares")
public class InternalFareController {

    private final FareService fareService;

    public InternalFareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    public FareEstimateResponse estimate(@Valid @RequestBody FareEstimateRequest request) {
        return fareService.estimateFare(request);
    }

    @PostMapping("/final")
    public ResponseEntity<FinalFareResponse> createFinalFare(@Valid @RequestBody FinalFareRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(FinalFareResponse.from(fareService.createFinalFare(request)));
    }
}
