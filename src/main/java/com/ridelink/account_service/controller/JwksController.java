package com.ridelink.account_service.controller;

import com.ridelink.account_service.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {

    @Autowired
    private JwtService jwtService;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        return jwtService.getJWKSet().toJSONObject();
    }
}