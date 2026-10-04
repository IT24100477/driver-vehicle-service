package com.ridelink.farepayment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "fare")
public class FareProperties {

    private BigDecimal baseFare = new BigDecimal("100");
    private BigDecimal perKmRate = new BigDecimal("80");

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getPerKmRate() {
        return perKmRate;
    }

    public void setPerKmRate(BigDecimal perKmRate) {
        this.perKmRate = perKmRate;
    }
}
