package com.ridelink.ridemanagementservice.exception;

import com.ridelink.ridemanagementservice.model.RideStatus;

public class InvalidRideStatusTransitionException extends RuntimeException {

    private final RideStatus currentStatus;
    private final RideStatus targetStatus;

    public InvalidRideStatusTransitionException(RideStatus currentStatus, RideStatus targetStatus) {
        super(String.format("Invalid ride status transition from %s to %s", currentStatus, targetStatus));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidRideStatusTransitionException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public RideStatus getCurrentStatus() {
        return currentStatus;
    }

    public RideStatus getTargetStatus() {
        return targetStatus;
    }
}
