package com.transformers.bumblebee.core;

/**
 * Enumerates all possible states of Bumblebee.
 * 
 * State transitions are strictly controlled by BumblebeeController
 * and guarded by canTransition() checks.
 */
public enum BumblebeeState {
    /**
     * Bumblebee is in vehicle form (Cheetah).
     * Player can drive, cannot attack or transform mid-air.
     */
    VEHICLE("Vehicle"),
    
    /**
     * Transformation from vehicle to robot is in progress.
     * Animation is playing, input is blocked, no movement allowed.
     */
    TRANSFORMING_TO_ROBOT("TransformingToRobot"),
    
    /**
     * Bumblebee is in robot form.
     * Player can walk, run, jump, attack, use weapons.
     */
    ROBOT("Robot"),
    
    /**
     * Transformation from robot to vehicle is in progress.
     * Animation is playing, input is blocked, no movement allowed.
     */
    TRANSFORMING_TO_VEHICLE("TransformingToVehicle");
    
    private final String displayName;
    
    BumblebeeState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this state is a transformation state (animation playing).
     */
    public boolean isTransforming() {
        return this == TRANSFORMING_TO_ROBOT || this == TRANSFORMING_TO_VEHICLE;
    }
    
    /**
     * Check if this state allows player input (movement, attack, etc).
     */
    public boolean allowsPlayerInput() {
        return this == ROBOT || this == VEHICLE;
    }
}
