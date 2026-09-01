package com.transformers.bumblebee.core;

import com.transformers.bumblebee.transformation.TransformationController;
import com.transformers.bumblebee.robot.RobotController;
import com.transformers.bumblebee.vehicle.VehicleController;
import com.transformers.bumblebee.combat.CombatController;
import com.transformers.bumblebee.input.InputController;
import com.transformers.bumblebee.input.InputAction;
import com.transformers.bumblebee.animation.AnimationController;
import com.transformers.bumblebee.effects.EffectsController;
import com.transformers.bumblebee.assets.AssetManager;
import com.transformers.bumblebee.config.Config;
import com.transformers.bumblebee.utils.Vector3;
import com.transformers.bumblebee.events.EventBus;
import com.transformers.bumblebee.events.BumblebeeEvent;

/**
 * Main orchestrator for the entire Bumblebee system.
 * 
 * This is a singleton that:
 * - Owns the current state machine
 * - Coordinates all child controllers
 * - Enforces state transition rules
 * - Handles input routing
 * - Manages lifecycle (init, update, cleanup)
 * 
 * Architecture:
 * BumblebeeController is the single source of truth for Bumblebee's state.
 * All subsystems (Transform, Robot, Vehicle, Combat, etc) are peers
 * that report to this controller.
 */
public class BumblebeeController {
    
    private static BumblebeeController instance;
    
    // Current state
    private BumblebeeState currentState = BumblebeeState.VEHICLE;
    
    // Child controllers
    private TransformationController transformationController;
    private RobotController robotController;
    private VehicleController vehicleController;
    private CombatController combatController;
    private InputController inputController;
    private AnimationController animationController;
    private EffectsController effectsController;
    private AssetManager assetManager;
    
    // Cached game entities
    private Object currentEntity; // Vehicle or Ped depending on state
    
    // Guard state
    private boolean initialized = false;
    
    private BumblebeeController() {
        // Private constructor for singleton
    }
    
    /**
     * Get the singleton instance.
     */
    public static BumblebeeController getInstance() {
        if (instance == null) {
            instance = new BumblebeeController();
        }
        return instance;
    }
    
    /**
     * Initialize all subsystems.
     * Must be called once before any update() calls.
     */
    public void initialize() {
        if (initialized) {
            return; // Already initialized
        }
        
        try {
            // Initialize in dependency order
            assetManager = new AssetManager();
            
            inputController = new InputController();
            inputController.registerCallback(InputAction.TRANSFORM, this::onTransformPressed);
            inputController.registerCallback(InputAction.ATTACK, this::onAttackPressed);
            inputController.registerCallback(InputAction.JUMP, this::onJumpPressed);
            
            animationController = new AnimationController(assetManager);
            effectsController = new EffectsController();
            
            transformationController = new TransformationController(
                assetManager,
                animationController,
                effectsController
            );
            
            vehicleController = new VehicleController();
            
            robotController = new RobotController(
                animationController,
                effectsController,
                assetManager
            );
            
            combatController = new CombatController(
                robotController,
                animationController,
                effectsController
            );
            
            // Subscribe to events
            EventBus.subscribe(BumblebeeEvent.TRANSFORMATION_COMPLETED, this::onTransformationComplete);
            
            initialized = true;
            
            // Fire initialization event
            EventBus.fire(BumblebeeEvent.STATE_CHANGED, new StateChangeEvent(null, currentState));
            
            System.out.println("[BumblebeeController] Initialized successfully");
            
        } catch (Exception e) {
            System.err.println("[BumblebeeController] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main update loop. Called every frame.
     */
    public void update(float deltaTime) {
        if (!initialized) {
            return;
        }
        
        try {
            // Update input first
            inputController.update();
            
            // Process current state
            switch (currentState) {
                case VEHICLE:
                    updateVehicleState(deltaTime);
                    break;
                    
                case TRANSFORMING_TO_ROBOT:
                case TRANSFORMING_TO_VEHICLE:
                    updateTransformationState(deltaTime);
                    break;
                    
                case ROBOT:
                    updateRobotState(deltaTime);
                    break;
            }
            
            // Update shared systems
            animationController.update(deltaTime);
            effectsController.update(deltaTime);
            
        } catch (Exception e) {
            System.err.println("[BumblebeeController] Update error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Clean up all resources. Call when mod is unloaded.
     */
    public void cleanup() {
        try {
            if (animationController != null) animationController.cleanup();
            if (effectsController != null) effectsController.cleanup();
            if (assetManager != null) assetManager.cleanup();
            if (robotController != null) robotController.cleanup();
            if (vehicleController != null) vehicleController.cleanup();
            
            initialized = false;
            System.out.println("[BumblebeeController] Cleaned up successfully");
            
        } catch (Exception e) {
            System.err.println("[BumblebeeController] Cleanup error: " + e.getMessage());
        }
    }
    
    // ========== State Management ==========
    
    /**
     * Get the current state.
     */
    public BumblebeeState getCurrentState() {
        return currentState;
    }
    
    /**
     * Transition to a new state (internal use).
     */
    private void setState(BumblebeeState newState) {
        if (newState == currentState) {
            return; // No change
        }
        
        BumblebeeState oldState = currentState;
        currentState = newState;
        
        System.out.println("[BumblebeeController] State transition: " + oldState + " -> " + newState);
        
        // Fire state change event
        EventBus.fire(BumblebeeEvent.STATE_CHANGED, new StateChangeEvent(oldState, newState));
    }
    
    // ========== Transformation Logic ==========
    
    /**
     * Initiate transformation. Can only happen from VEHICLE or ROBOT states.
     */
    public void beginTransformation() {
        // Check if we can transform
        if (!canTransform()) {
            System.out.println("[BumblebeeController] Transformation blocked by guard conditions");
            effectsController.playSound("error_transform_blocked", getEntityPosition());
            return;
        }
        
        // Determine direction
        TransformationController.TransformDirection direction;
        BumblebeeState nextState;
        
        if (currentState == BumblebeeState.VEHICLE) {
            direction = TransformationController.TransformDirection.TO_ROBOT;
            nextState = BumblebeeState.TRANSFORMING_TO_ROBOT;
        } else if (currentState == BumblebeeState.ROBOT) {
            direction = TransformationController.TransformDirection.TO_VEHICLE;
            nextState = BumblebeeState.TRANSFORMING_TO_VEHICLE;
        } else {
            // Already transforming or invalid state
            return;
        }
        
        // Start transformation
        setState(nextState);
        transformationController.startTransformation(direction, currentEntity);
        
        // Fire event
        EventBus.fire(BumblebeeEvent.TRANSFORMATION_STARTED, direction);
    }
    
    /**
     * Check if transformation is allowed right now.
     */
    private boolean canTransform() {
        // Cannot transform while already transforming
        if (currentState.isTransforming()) {
            return false;
        }
        
        // Cannot transform if assets not ready
        if (!assetManager.isRobotModelLoaded()) {
            return false;
        }
        
        // Check guard conditions (physics, collision, etc)
        // These are delegated to transformation controller or vehicle/robot controller
        // For now, assume they're valid
        
        return true;
    }
    
    /**
     * Called when transformation animation completes.
     */
    private void onTransformationComplete(Object data) {
        if (currentState == BumblebeeState.TRANSFORMING_TO_ROBOT) {
            setState(BumblebeeState.ROBOT);
            System.out.println("[BumblebeeController] Transformation to robot complete");
            
        } else if (currentState == BumblebeeState.TRANSFORMING_TO_VEHICLE) {
            setState(BumblebeeState.VEHICLE);
            System.out.println("[BumblebeeController] Transformation to vehicle complete");
        }
    }
    
    // ========== State-Specific Updates ==========
    
    private void updateVehicleState(float deltaTime) {
        // Player is in the Cheetah vehicle
        // Let VehicleController handle movement, camera, etc
        if (vehicleController != null) {
            vehicleController.update(deltaTime);
        }
    }
    
    private void updateTransformationState(float deltaTime) {
        // Transformation animation is playing
        // TransformationController handles everything
        if (transformationController != null) {
            transformationController.update(deltaTime);
        }
    }
    
    private void updateRobotState(float deltaTime) {
        // Player is the robot
        // Update movement, animation, combat systems
        
        if (robotController != null) {
            // Apply input to robot movement
            Vector3 moveInput = inputController.getMovementInput();
            boolean running = inputController.isActionPressed(InputAction.RUN);
            robotController.setMovementInput(moveInput, running);
            
            // Update robot physics and animation
            robotController.update(deltaTime);
        }
        
        if (combatController != null) {
            combatController.update(deltaTime);
        }
    }
    
    // ========== Input Callbacks ==========
    
    private void onTransformPressed() {
        if (currentState.allowsPlayerInput()) {
            beginTransformation();
        }
    }
    
    private void onAttackPressed() {
        if (currentState == BumblebeeState.ROBOT && combatController != null) {
            combatController.meleeAttack();
        }
    }
    
    private void onJumpPressed() {
        if (currentState == BumblebeeState.ROBOT && robotController != null) {
            robotController.jump();
        }
    }
    
    // ========== Damage & Health ==========
    
    /**
     * Apply damage to Bumblebee (only when in robot form).
     */
    public void takeDamage(int amount, Vector3 source) {
        if (currentState == BumblebeeState.ROBOT && combatController != null) {
            combatController.takeDamage(amount, source);
            EventBus.fire(BumblebeeEvent.ROBOT_HIT, new DamageEvent(amount, source));
        }
    }
    
    /**
     * Get current health.
     */
    public int getHealth() {
        if (combatController != null) {
            return combatController.getHealth();
        }
        return 0;
    }
    
    // ========== Utility Methods ==========
    
    /**
     * Get current position (vehicle or robot).
     */
    public Vector3 getEntityPosition() {
        if (currentState == BumblebeeState.VEHICLE || currentState == BumblebeeState.TRANSFORMING_TO_VEHICLE) {
            if (vehicleController != null) {
                return vehicleController.getPosition();
            }
        } else if (currentState == BumblebeeState.ROBOT || currentState == BumblebeeState.TRANSFORMING_TO_ROBOT) {
            if (robotController != null) {
                return robotController.getPosition();
            }
        }
        return Vector3.ZERO;
    }
    
    /**
     * Set the game entity reference (called by integration layer).
     */
    public void setCurrentEntity(Object entity) {
        this.currentEntity = entity;
    }
    
    // ========== Helper Classes ==========
    
    public static class StateChangeEvent {
        public BumblebeeState oldState;
        public BumblebeeState newState;
        
        public StateChangeEvent(BumblebeeState oldState, BumblebeeState newState) {
            this.oldState = oldState;
            this.newState = newState;
        }
    }
    
    public static class DamageEvent {
        public int amount;
        public Vector3 source;
        
        public DamageEvent(int amount, Vector3 source) {
            this.amount = amount;
            this.source = source;
        }
    }
}
