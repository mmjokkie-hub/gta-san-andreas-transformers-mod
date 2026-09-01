package com.transformers.bumblebee.robot;

import com.transformers.bumblebee.animation.AnimationController;
import com.transformers.bumblebee.assets.AssetManager;
import com.transformers.bumblebee.effects.EffectsController;
import com.transformers.bumblebee.utils.Vector3;

/**
 * Controls the robot form of Bumblebee.
 * 
 * Handles:
 * - Movement (walk, run, turn)
 * - Animation playback (idle, walk cycle, run cycle)
 * - Physics (gravity, ground detection)
 * - Rotation/facing direction
 * - Jumping
 * 
 * The robot is significantly larger than a normal GTA pedestrian
 * and feels heavy with mechanical weight.
 */
public class RobotController {
    
    private final AnimationController animationController;
    private final EffectsController effectsController;
    private final AssetManager assetManager;
    
    // Robot properties (loaded from config)
    private float moveSpeed = 1.5f;        // Units per second
    private float runSpeed = 2.5f;
    private float rotationSpeed = 180.0f;  // Degrees per second
    private float jumpForce = 10.0f;       // Upward velocity when jumping
    private float gravity = 9.8f;
    private int maxHealth = 200;
    
    // Current state
    private Vector3 position = Vector3.ZERO;
    private Vector3 velocity = Vector3.ZERO;
    private float yaw = 0.0f;              // Facing angle in degrees
    private int currentHealth = maxHealth;
    private boolean isGrounded = false;
    private boolean isDead = false;
    
    // Movement input
    private Vector3 movementInput = Vector3.ZERO;
    private boolean isRunning = false;
    
    // Animation state
    private String currentAnimation = "idle";
    private boolean isPlaying = false;
    
    public RobotController(
            AnimationController animationController,
            EffectsController effectsController,
            AssetManager assetManager) {
        this.animationController = animationController;
        this.effectsController = effectsController;
        this.assetManager = assetManager;
    }
    
    /**
     * Update robot every frame.
     */
    public void update(float deltaTime) {
        if (isDead) {
            return; // Don't update dead robot
        }
        
        // Apply gravity
        applyGravity(deltaTime);
        
        // Apply movement
        applyMovement(deltaTime);
        
        // Update animation based on movement state
        updateAnimation();
    }
    
    /**
     * Set movement input from player (e.g., from gamepad stick).
     * Direction should be normalized.
     */
    public void setMovementInput(Vector3 direction, boolean running) {
        this.movementInput = direction;
        this.isRunning = running;
    }
    
    /**
     * Set facing direction (yaw in degrees).
     */
    public void setFacingDirection(float yaw) {
        this.yaw = yaw % 360.0f; // Normalize to 0-360
    }
    
    /**
     * Make the robot jump.
     */
    public void jump() {
        if (isGrounded && !isDead) {
            velocity.y = jumpForce;
            isGrounded = false;
            
            // Play jump sound
            effectsController.playSound("robot_jump", position);
        }
    }
    
    /**
     * Apply damage to robot.
     */
    public void takeDamage(int amount, Vector3 source) {
        if (isDead) {
            return;
        }
        
        currentHealth -= amount;
        
        // Play hit sound
        effectsController.playSound("robot_hit", position);
        
        // Play hit animation
        Vector3 direction = position.subtract(source).normalized();
        if (direction.x * direction.x + direction.z * direction.z > 0.5f) {
            animationController.playAnimation("hit_front", false, 0.1f);
        } else {
            animationController.playAnimation("hit_back", false, 0.1f);
        }
        
        if (currentHealth <= 0) {
            die();
        }
    }
    
    /**
     * Kill the robot (play death animation).
     */
    private void die() {
        isDead = true;
        currentHealth = 0;
        
        // Play death animation
        animationController.playAnimation("death", false, 0.1f);
        
        // Play death sound
        effectsController.playSound("robot_death", position);
        
        // Create death effect (explosion, sparks)
        effectsController.spawnExplosion(position, 5.0f, 100);
    }
    
    // ========== Private Methods ==========
    
    private void applyGravity(float deltaTime) {
        if (!isGrounded) {
            velocity.y -= gravity * deltaTime;
        }
        
        // Simple ground detection - in real implementation, raycast to terrain
        if (position.y <= 0.1f) {
            position.y = 0.0f;
            velocity.y = 0.0f;
            isGrounded = true;
        } else {
            isGrounded = false;
        }
    }
    
    private void applyMovement(float deltaTime) {
        // Determine movement speed
        float currentSpeed = isRunning ? runSpeed : moveSpeed;
        
        // Apply input to velocity (horizontal plane only)
        if (movementInput.length() > 0.1f) {
            Vector3 moveVector = movementInput.normalized().multiply(currentSpeed * deltaTime);
            velocity.x += moveVector.x;
            velocity.z += moveVector.z;
            
            // Update facing direction based on movement
            setFacingDirection(getAngleTo(movementInput));
        }
        
        // Apply velocity to position
        position = position.add(velocity.multiply(deltaTime));
        
        // Simple velocity damping when no input
        if (movementInput.length() < 0.1f) {
            velocity.x *= 0.95f; // Friction
            velocity.z *= 0.95f;
        }
    }
    
    private void updateAnimation() {
        String desiredAnimation;
        
        if (isDead) {
            return; // Don't change animation if dead
        }
        
        // Determine which animation to play
        if (movementInput.length() > 0.1f) {
            if (isRunning) {
                desiredAnimation = "run";
            } else {
                desiredAnimation = "walk";
            }
        } else {
            desiredAnimation = "idle";
        }
        
        // Only change animation if different
        if (!desiredAnimation.equals(currentAnimation)) {
            animationController.playAnimation(desiredAnimation, true, 0.3f);
            currentAnimation = desiredAnimation;
        }
    }
    
    private float getAngleTo(Vector3 direction) {
        // Convert direction vector to yaw angle
        return (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
    }
    
    // ========== Getters ==========
    
    public Vector3 getPosition() {
        return position.copy();
    }
    
    public int getHealth() {
        return currentHealth;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public void cleanup() {
        // No cleanup needed for now
    }
}
