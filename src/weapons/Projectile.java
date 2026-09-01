package com.transformers.bumblebee.weapons;

import com.transformers.bumblebee.utils.Vector3;

/**
 * Represents a projectile fired by a weapon.
 * 
 * Projectiles are pooled for performance.
 * Each frame they move and check for collisions.
 */
public class Projectile {
    
    private Vector3 position;
    private Vector3 velocity;
    private int damage;
    private float lifetime;
    private float age = 0.0f;
    private boolean active = true;
    
    public Projectile(Vector3 position, Vector3 velocity, int damage, float lifetime) {
        this.position = position.copy();
        this.velocity = velocity.copy();
        this.damage = damage;
        this.lifetime = lifetime;
    }
    
    /**
     * Update projectile every frame.
     */
    public void update(float deltaTime) {
        if (!active) {
            return;
        }
        
        // Move projectile
        position = position.add(velocity.multiply(deltaTime));
        age += deltaTime;
        
        // Expire when lifetime reached
        if (age >= lifetime) {
            active = false;
        }
        
        // [TODO] Check collision with game objects
        // if (checkCollision()) { onHit(); }
    }
    
    /**
     * Get current position.
     */
    public Vector3 getPosition() {
        return position.copy();
    }
    
    /**
     * Check if projectile is still active.
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Deactivate projectile (for pooling).
     */
    public void deactivate() {
        active = false;
    }
    
    /**
     * Reset projectile for reuse from pool.
     */
    public void reset(Vector3 newPosition, Vector3 newVelocity, int newDamage) {
        this.position = newPosition.copy();
        this.velocity = newVelocity.copy();
        this.damage = newDamage;
        this.age = 0.0f;
        this.active = true;
    }
    
    public int getDamage() {
        return damage;
    }
}
