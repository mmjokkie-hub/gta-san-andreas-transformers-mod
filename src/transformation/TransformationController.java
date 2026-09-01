package com.transformers.bumblebee.transformation;

import com.transformers.bumblebee.animation.AnimationController;
import com.transformers.bumblebee.assets.AssetManager;
import com.transformers.bumblebee.effects.EffectsController;
import com.transformers.bumblebee.events.EventBus;
import com.transformers.bumblebee.events.BumblebeeEvent;
import com.transformers.bumblebee.utils.Vector3;

/**
 * Controls the transformation sequence from vehicle to robot and back.
 * 
 * Transformation is broken into distinct phases:
 * 1. PRE_TRANSFORM: Freeze input, disable vehicle controls
 * 2. ANIMATION_PLAY: Play transformation animation, swap model at midpoint
 * 3. POST_TRANSFORM: Restore physics, enable input
 * 
 * The transformation sequence is not a simple model swap.
 * It's a choreographed animation with:
 * - Vehicle parts unfolding
 * - Wheels repositioning
 * - Robot limbs deploying
 * - Mechanical sound effects and particle effects
 */
public class TransformationController {
    
    public enum TransformDirection {
        TO_ROBOT("transform_to_robot", 3.0f),    // Animation name and duration
        TO_VEHICLE("transform_to_vehicle", 3.0f);
        
        public final String animationName;
        public final float duration;
        
        TransformDirection(String animationName, float duration) {
            this.animationName = animationName;
            this.duration = duration;
        }
    }
    
    private enum TransformPhase {
        IDLE,           // Not transforming
        PRE_TRANSFORM,  // Freeze input, setup
        ANIMATION_PLAY, // Play animation
        MODEL_SWAP,     // Swap vehicle model for robot model (happens mid-animation)
        POST_TRANSFORM  // Restore controls, fire completion event
    }
    
    // Dependencies
    private final AssetManager assetManager;
    private final AnimationController animationController;
    private final EffectsController effectsController;
    
    // State
    private TransformPhase currentPhase = TransformPhase.IDLE;
    private TransformDirection direction;
    private float phaseTimer = 0.0f;
    private float modelSwapTime; // When to swap model (halfway through animation)
    private Object transformingEntity; // Vehicle or Ped being transformed
    private boolean modelSwapped = false;
    
    public TransformationController(
            AssetManager assetManager,
            AnimationController animationController,
            EffectsController effectsController) {
        this.assetManager = assetManager;
        this.animationController = animationController;
        this.effectsController = effectsController;
    }
    
    /**
     * Start a transformation sequence.
     */
    public void startTransformation(TransformDirection direction, Object entity) {
        if (currentPhase != TransformPhase.IDLE) {
            System.out.println("[TransformationController] Already transforming, ignoring start request");
            return;
        }
        
        this.direction = direction;
        this.transformingEntity = entity;
        this.phaseTimer = 0.0f;
        this.modelSwapped = false;
        this.modelSwapTime = direction.duration / 2.0f; // Swap at halfway point
        
        // Enter pre-transform phase
        enterPreTransformPhase();
        
        System.out.println("[TransformationController] Starting transformation: " + direction);
    }
    
    /**
     * Update transformation state every frame.
     */
    public void update(float deltaTime) {
        if (currentPhase == TransformPhase.IDLE) {
            return; // Not transforming
        }
        
        phaseTimer += deltaTime;
        
        switch (currentPhase) {
            case PRE_TRANSFORM:
                updatePreTransformPhase(deltaTime);
                break;
            case ANIMATION_PLAY:
                updateAnimationPhase(deltaTime);
                break;
            case MODEL_SWAP:
                updateModelSwapPhase(deltaTime);
                break;
            case POST_TRANSFORM:
                updatePostTransformPhase(deltaTime);
                break;
        }
    }
    
    /**
     * Check if currently transforming.
     */
    public boolean isTransforming() {
        return currentPhase != TransformPhase.IDLE;
    }
    
    /**
     * Get transformation progress (0.0 to 1.0).
     */
    public float getTransformationProgress() {
        if (direction == null) {
            return 0.0f;
        }
        return Math.min(phaseTimer / direction.duration, 1.0f);
    }
    
    // ========== Phase Implementations ==========
    
    private void enterPreTransformPhase() {
        currentPhase = TransformPhase.PRE_TRANSFORM;
        phaseTimer = 0.0f;
        
        // Play start sound
        effectsController.playSound("transform_start", getEntityPosition());
        
        // Disable movement/input (handled by BumblebeeController)
        // Stop current animation if any
        animationController.stopAnimation(0.2f);
    }
    
    private void updatePreTransformPhase(float deltaTime) {
        // Brief pause before animation starts (allows sound to begin)
        if (phaseTimer >= 0.3f) {
            enterAnimationPhase();
        }
    }
    
    private void enterAnimationPhase() {
        currentPhase = TransformPhase.ANIMATION_PLAY;
        phaseTimer = 0.0f;
        
        // Play transformation animation
        animationController.playAnimation(direction.animationName, false, 0.3f);
        
        // Create transformation effect (dust, sparks, etc)
        effectsController.createParticleEffect("transform_dust", getEntityPosition());
    }
    
    private void updateAnimationPhase(float deltaTime) {
        // Check if it's time to swap the model
        if (!modelSwapped && phaseTimer >= modelSwapTime) {
            performModelSwap();
        }
        
        // Animation complete?
        if (phaseTimer >= direction.duration) {
            enterPostTransformPhase();
        }
    }
    
    private void performModelSwap() {
        modelSwapped = true;
        currentPhase = TransformPhase.MODEL_SWAP;
        
        try {
            if (direction == TransformDirection.TO_ROBOT) {
                // Swap Cheetah vehicle model for Bumblebee robot model
                swapToRobotModel();
            } else {
                // Swap Bumblebee robot model back to Cheetah vehicle model
                swapToVehicleModel();
            }
            
            System.out.println("[TransformationController] Model swap complete");
            
            // Play midpoint effect (e.g., brief explosion, bright flash)
            effectsController.createParticleEffect("transform_midpoint_flash", getEntityPosition());
            
        } catch (Exception e) {
            System.err.println("[TransformationController] Model swap failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void swapToRobotModel() {
        // [INTEGRATION POINT]
        // Replace vehicle model with robot model
        // This involves:
        // 1. Despawn the Cheetah vehicle
        // 2. Create a Bumblebee robot ped at the same location
        // 3. Make the robot the player-controlled character
        
        // Placeholder for now - actual implementation goes in GameEngineBridge
        System.out.println("[TransformationController] Swapping to robot model");
    }
    
    private void swapToVehicleModel() {
        // [INTEGRATION POINT]
        // Replace robot ped with vehicle model
        // This involves:
        // 1. Despawn the Bumblebee robot
        // 2. Create a Cheetah vehicle at the same location
        // 3. Warp the player into the vehicle
        
        // Placeholder for now - actual implementation goes in GameEngineBridge
        System.out.println("[TransformationController] Swapping to vehicle model");
    }
    
    private void enterPostTransformPhase() {
        currentPhase = TransformPhase.POST_TRANSFORM;
        phaseTimer = 0.0f;
        
        // Play complete sound
        effectsController.playSound("transform_complete", getEntityPosition());
        
        // Restore physics and input (handled by BumblebeeController)
        // Enable movement controls
    }
    
    private void updatePostTransformPhase(float deltaTime) {
        // Brief delay to ensure sound plays and effects are visible
        if (phaseTimer >= 0.5f) {
            completeTransformation();
        }
    }
    
    private void completeTransformation() {
        System.out.println("[TransformationController] Transformation complete");
        
        // Fire completion event
        EventBus.fire(BumblebeeEvent.TRANSFORMATION_COMPLETED, direction);
        
        // Reset state
        currentPhase = TransformPhase.IDLE;
        direction = null;
        transformingEntity = null;
        modelSwapped = false;
    }
    
    // ========== Utility Methods ==========
    
    private Vector3 getEntityPosition() {
        // [INTEGRATION POINT] Get position from vehicle or ped
        return Vector3.ZERO; // Placeholder
    }
}
