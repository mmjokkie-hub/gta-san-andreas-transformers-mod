package com.transformers.bumblebee.core;

/**
 * Bumblebee implementation of the Transformer interface.
 * 
 * Bumblebee is the first Transformer in the mod.
 * Vehicle form: Cheetah
 * Robot form: Custom Bumblebee model
 * 
 * This class bridges the system-wide Transformer interface with
 * the BumblebeeController singleton.
 */
public class BumblebeeTransformer implements Transformer {
    
    private static final String NAME = "Bumblebee";
    private static final int VEHICLE_MODEL_ID = 168; // GTA SA Cheetah model ID
    private static final int ROBOT_MODEL_ID = 999;   // Custom Bumblebee robot model (placeholder)
    
    private BumblebeeController controller;
    
    public BumblebeeTransformer() {
        this.controller = BumblebeeController.getInstance();
    }
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public int getVehicleModelId() {
        return VEHICLE_MODEL_ID;
    }
    
    @Override
    public int getRobotModelId() {
        return ROBOT_MODEL_ID;
    }
    
    @Override
    public void initialize() {
        controller.initialize();
    }
    
    @Override
    public void update(float deltaTime) {
        controller.update(deltaTime);
    }
    
    @Override
    public void cleanup() {
        controller.cleanup();
    }
    
    @Override
    public BumblebeeState getState() {
        return controller.getCurrentState();
    }
    
    @Override
    public void transform() {
        controller.beginTransformation();
    }
}
