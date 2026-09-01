package com.transformers.bumblebee.input;

import java.util.*;

/**
 * Maps input actions to raw input sources (keys, gamepad buttons, touch).
 * 
 * This abstraction allows input configuration to be changed without
 * modifying game code. All mappings come from JSON config files.
 */
public class InputMapping {
    
    // Map each action to its input sources
    private Map<InputAction, List<InputSource>> actionMap = new HashMap<>();
    
    public InputMapping() {
        // Default mappings (can be overridden by config)
        setupDefaults();
    }
    
    /**
     * Get all input sources mapped to an action.
     */
    public List<InputSource> getSourcesForAction(InputAction action) {
        return actionMap.getOrDefault(action, new ArrayList<>());
    }
    
    /**
     * Map an action to an input source.
     */
    public void mapActionToSource(InputAction action, InputSource source) {
        actionMap.computeIfAbsent(action, k -> new ArrayList<>()).add(source);
    }
    
    /**
     * Clear all mappings for an action.
     */
    public void clearAction(InputAction action) {
        actionMap.remove(action);
    }
    
    private void setupDefaults() {
        // Robot/Transform
        mapActionToSource(InputAction.TRANSFORM, new KeyboardInput(KeyCode.SPACE));
        mapActionToSource(InputAction.JUMP, new KeyboardInput(KeyCode.SPACE));
        mapActionToSource(InputAction.RUN, new KeyboardInput(KeyCode.SHIFT));
        
        // Combat
        mapActionToSource(InputAction.ATTACK, new MouseInput(MouseButton.LEFT));
        mapActionToSource(InputAction.AIM, new MouseInput(MouseButton.RIGHT));
        mapActionToSource(InputAction.WEAPON, new KeyboardInput(KeyCode.W));
        
        // Movement (gamepad stick is handled separately)
        mapActionToSource(InputAction.MOVE_FORWARD, new KeyboardInput(KeyCode.W));
        mapActionToSource(InputAction.MOVE_BACKWARD, new KeyboardInput(KeyCode.S));
        mapActionToSource(InputAction.MOVE_LEFT, new KeyboardInput(KeyCode.A));
        mapActionToSource(InputAction.MOVE_RIGHT, new KeyboardInput(KeyCode.D));
        
        // Vehicle
        mapActionToSource(InputAction.ACCELERATE, new KeyboardInput(KeyCode.W));
        mapActionToSource(InputAction.BRAKE, new KeyboardInput(KeyCode.S));
        
        // UI
        mapActionToSource(InputAction.MENU, new KeyboardInput(KeyCode.ESCAPE));
        mapActionToSource(InputAction.PAUSE, new KeyboardInput(KeyCode.P));
    }
    
    /**
     * Load mappings from JSON config file.
     */
    public static InputMapping loadFromConfig(String configPath) {
        // [TODO] Parse JSON and create InputMapping
        // For now, return defaults
        return new InputMapping();
    }
    
    // ========== Input Source Abstractions ==========
    
    public abstract static class InputSource {
        abstract boolean isPressed();
    }
    
    public static class KeyboardInput extends InputSource {
        private KeyCode keyCode;
        
        public KeyboardInput(KeyCode keyCode) {
            this.keyCode = keyCode;
        }
        
        @Override
        boolean isPressed() {
            // [INTEGRATION POINT] Check if key is pressed via game engine
            return false; // Placeholder
        }
    }
    
    public static class MouseInput extends InputSource {
        private MouseButton button;
        
        public MouseInput(MouseButton button) {
            this.button = button;
        }
        
        @Override
        boolean isPressed() {
            // [INTEGRATION POINT] Check if mouse button is pressed
            return false; // Placeholder
        }
    }
    
    public static class TouchInput extends InputSource {
        private float touchX, touchY;
        private float width, height;
        
        public TouchInput(float x, float y, float w, float h) {
            this.touchX = x;
            this.touchY = y;
            this.width = w;
            this.height = h;
        }
        
        @Override
        boolean isPressed() {
            // [INTEGRATION POINT] Check if touch point is active in this region
            return false; // Placeholder
        }
    }
    
    public static class GamepadInput extends InputSource {
        private GamepadButton button;
        
        public GamepadInput(GamepadButton button) {
            this.button = button;
        }
        
        @Override
        boolean isPressed() {
            // [INTEGRATION POINT] Check if gamepad button is pressed
            return false; // Placeholder
        }
    }
    
    public enum KeyCode {
        W, A, S, D, SPACE, SHIFT, ESCAPE, P, E
    }
    
    public enum MouseButton {
        LEFT, RIGHT, MIDDLE
    }
    
    public enum GamepadButton {
        A, B, X, Y, LT, RT, LB, RB, START, BACK
    }
}
