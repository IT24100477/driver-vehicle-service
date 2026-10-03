package lk.ridelink.fare_payment_service.controller;

import jakarta.validation.Valid;
import lk.ridelink.fare_payment_service.fare.FareEstimateRequest;
import lk.ridelink.fare_payment_service.fare.FareEstimateResponse;
import lk.ridelink.fare_payment_service.service.FareService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<FareEstimateResponse> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        FareEstimateResponse response = fareService.calculateEstimate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/final")
    public ResponseEntity<FareEstimateResponse> calculateFinalFare(@Valid @RequestBody FareEstimateRequest request) {
        FareEstimateResponse response = fareService.calculateFinalFare(request);
        return ResponseEntity.ok(response);
    }
}
