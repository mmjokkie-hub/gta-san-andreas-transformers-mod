package com.transformers.bumblebee.core;

/**
 * Base interface for all Transformer characters (Bumblebee, Optimus Prime, etc).
 * 
 * This allows the system to be extended with new Transformers without
 * rewriting the core logic. Currently only Bumblebee is implemented.
 * 
 * Future Transformers can implement this interface with their own
 * TransformationControllers, RobotControllers, etc.
 */
public interface Transformer {
    
    /**
     * Get the name of this Transformer (e.g., "Bumblebee", "Optimus Prime").
     */
    String getName();
    
    /**
     * Get the vehicle model ID this Transformer uses.
     */
    int getVehicleModelId();
    
    /**
     * Get the robot model ID this Transformer uses.
     */
    int getRobotModelId();
    
    /**
     * Initialize this Transformer's systems.
     */
    void initialize();
    
    /**
     * Update this Transformer every frame.
     */
    void update(float deltaTime);
    
    /**
     * Clean up this Transformer's resources.
     */
    void cleanup();
    
    /**
     * Get current state.
     */
    BumblebeeState getState();
    
    /**
     * Begin transformation.
     */
    void transform();
}
