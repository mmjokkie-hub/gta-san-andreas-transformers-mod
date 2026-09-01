package com.transformers.bumblebee.input;

import com.transformers.bumblebee.utils.Vector2;
import java.util.*;

/**
 * Handles all input processing and action mapping.
 * 
 * Responsibilities:
 * - Read raw input (keyboard, gamepad, touch)
 * - Map raw input to logical actions via InputMapping
 * - Fire input action callbacks
 * - Block inputs during transformations
 * 
 * This abstraction means the rest of the system never touches
 * raw input directly.
 */
public class InputController {
    
    private InputMapping inputMapping;
    private Map<InputAction, InputCallback> callbacks = new HashMap<>();
    private boolean inputEnabled = true;
    
    // Analog input (gamepad stick, touch drag)
    private Vector2 movementAxis = Vector2.ZERO;
    private Vector2 lookAxis = Vector2.ZERO;
    
    public InputController() {
        this.inputMapping = InputMapping.loadFromConfig("assets/config/input_mapping.json");
    }
    
    /**
     * Update input state every frame.
     */
    public void update() {
        if (!inputEnabled) {
            return; // Input is blocked (e.g., during transformation)
        }
        
        // Read raw input and check for action presses
        for (InputAction action : InputAction.values()) {
            if (isActionPressed(action)) {
                fireCallback(action);
            }
        }
        
        // Read analog input
        readMovementAxis();
        readLookAxis();
    }
    
    /**
     * Check if an action is currently pressed.
     */
    public boolean isActionPressed(InputAction action) {
        List<InputMapping.InputSource> sources = inputMapping.getSourcesForAction(action);
        
        for (InputMapping.InputSource source : sources) {
            if (source.isPressed()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get movement input (analog stick or WASD).
     * Returns normalized vector.
     */
    public Vector2 getMovementAxis() {
        return movementAxis.copy();
    }
    
    /**
     * Convert to Vector3 for robot movement.
     */
    public com.transformers.bumblebee.utils.Vector3 getMovementInput() {
        return new com.transformers.bumblebee.utils.Vector3(
            movementAxis.x,
            0,
            movementAxis.y
        );
    }
    
    /**
     * Get look/aim input.
     */
    public Vector2 getLookAxis() {
        return lookAxis.copy();
    }
    
    /**
     * Register a callback for an input action.
     */
    public void registerCallback(InputAction action, InputCallback callback) {
        callbacks.put(action, callback);
    }
    
    /**
     * Enable/disable all input.
     * Used to block input during transformations.
     */
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
        
        if (!enabled) {
            // Clear movement when disabling
            movementAxis = Vector2.ZERO;
            lookAxis = Vector2.ZERO;
        }
    }
    
    public boolean isInputEnabled() {
        return inputEnabled;
    }
    
    // ========== Private Methods ==========
    
    private void fireCallback(InputAction action) {
        InputCallback callback = callbacks.get(action);
        if (callback != null) {
            try {
                callback.onAction();
            } catch (Exception e) {
                System.err.println("[InputController] Callback error for " + action + ": " + e.getMessage());
            }
        }
    }
    
    private void readMovementAxis() {
        // [INTEGRATION POINT] Read gamepad left stick
        Vector2 gamepadInput = readGamepadAxis(GamepadAxis.LEFT_X, GamepadAxis.LEFT_Y);
        
        if (gamepadInput.length() > 0.1f) {
            movementAxis = gamepadInput.normalized();
            return; // Prefer gamepad input
        }
        
        // Fall back to keyboard WASD
        Vector2 keyboardInput = Vector2.ZERO;
        if (isActionPressed(InputAction.MOVE_FORWARD)) {
            keyboardInput.y += 1;
        }
        if (isActionPressed(InputAction.MOVE_BACKWARD)) {
            keyboardInput.y -= 1;
        }
        if (isActionPressed(InputAction.MOVE_LEFT)) {
            keyboardInput.x -= 1;
        }
        if (isActionPressed(InputAction.MOVE_RIGHT)) {
            keyboardInput.x += 1;
        }
        
        movementAxis = keyboardInput.length() > 0.1f ? keyboardInput.normalized() : Vector2.ZERO;
    }
    
    private void readLookAxis() {
        // [INTEGRATION POINT] Read gamepad right stick
        lookAxis = readGamepadAxis(GamepadAxis.RIGHT_X, GamepadAxis.RIGHT_Y);
    }
    
    private Vector2 readGamepadAxis(GamepadAxis axisX, GamepadAxis axisY) {
        // [INTEGRATION POINT] Read gamepad analog axes
        // Placeholder returns zero
        return Vector2.ZERO;
    }
    
    // ========== Helper Types ==========
    
    @FunctionalInterface
    public interface InputCallback {
        void onAction();
    }
    
    enum GamepadAxis {
        LEFT_X, LEFT_Y, RIGHT_X, RIGHT_Y,
        TRIGGER_LEFT, TRIGGER_RIGHT
    }
}
