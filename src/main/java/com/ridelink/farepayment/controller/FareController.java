package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.dto.FareEstimateResponse;
import com.ridelink.farepayment.dto.FinalFareResponse;
import com.ridelink.farepayment.service.FareService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    public FareEstimateResponse estimate(@Valid @RequestBody FareEstimateRequest request) {
        return fareService.estimateFare(request);
    }

    @GetMapping("/rides/{rideId}/final")
    public FinalFareResponse getFinalFare(@PathVariable Long rideId) {
        return FinalFareResponse.from(fareService.getFinalFareByRideId(rideId));
    }
}
