package com.transformers.bumblebee.input;

/**
 * Enumerates all possible input actions.
 * 
 * Each action can be mapped to different input sources
 * (keyboard, gamepad, touch buttons) via InputMapping config.
 */
public enum InputAction {
    // Movement
    MOVE_FORWARD,
    MOVE_BACKWARD,
    MOVE_LEFT,
    MOVE_RIGHT,
    
    // Combat
    ATTACK,      // Melee punch/kick
    AIM,         // Hold to aim weapon
    WEAPON,      // Switch weapon
    
    // Robot/Vehicle
    TRANSFORM,   // Toggle vehicle/robot form
    JUMP,        // Jump (robot only)
    RUN,         // Hold to run
    
    // Vehicle
    ACCELERATE,  // Vehicle only
    BRAKE,       // Vehicle only
    HORN,        // Vehicle only
    
    // UI
    MENU,
    PAUSE;
    
    public String getDisplayName() {
        return this.name().replace('_', ' ');
    }
}
