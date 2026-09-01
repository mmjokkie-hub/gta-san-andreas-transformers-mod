package com.transformers.bumblebee.weapons;

import com.transformers.bumblebee.utils.Vector3;
import java.util.*;

/**
 * Manages weapon definitions and firing logic.
 * 
 * Responsibilities:
 * - Load weapon configurations from JSON
 * - Handle weapon switching
 * - Create projectiles with correct parameters
 * - Manage ammo counts
 * - Apply weapon cooldown/fire rate
 */
public class WeaponController {
    
    private Map<String, WeaponDefinition> weapons = new HashMap<>();
    private String currentWeapon = "default_cannon";
    private Map<String, Integer> ammoCount = new HashMap<>();
    private float fireCooldown = 0.0f;
    
    public WeaponController() {
        loadWeaponDefinitions();
    }
    
    /**
     * Fire the current weapon.
     */
    public void fireWeapon(Vector3 position, Vector3 direction) {
        if (fireCooldown > 0) {
            return; // Still cooling down
        }
        
        WeaponDefinition weaponDef = weapons.get(currentWeapon);
        if (weaponDef == null) {
            return;
        }
        
        // Check ammo
        int ammo = ammoCount.getOrDefault(currentWeapon, 0);
        if (weaponDef.requiresAmmo && ammo <= 0) {
            return; // Out of ammo
        }
        
        // Create projectile
        Projectile projectile = new Projectile(
            position,
            direction.normalized().multiply(weaponDef.projectileSpeed),
            weaponDef.damage,
            weaponDef.lifetime
        );
        
        // Reduce ammo
        if (weaponDef.requiresAmmo) {
            ammoCount.put(currentWeapon, ammo - 1);
        }
        
        // Set cooldown
        fireCooldown = 1.0f / weaponDef.fireRate; // Fire rate in shots per second
        
        System.out.println("[WeaponController] Fired " + currentWeapon + ", ammo: " + 
                          ammoCount.getOrDefault(currentWeapon, -1));
    }
    
    /**
     * Update weapon state every frame.
     */
    public void update(float deltaTime) {
        if (fireCooldown > 0) {
            fireCooldown -= deltaTime;
        }
    }
    
    /**
     * Switch to a different weapon.
     */
    public void switchWeapon(String weaponType) {
        if (weapons.containsKey(weaponType)) {
            currentWeapon = weaponType;
            System.out.println("[WeaponController] Switched to " + weaponType);
        }
    }
    
    /**
     * Add ammo for a weapon type.
     */
    public void addAmmo(String weaponType, int amount) {
        int current = ammoCount.getOrDefault(weaponType, 0);
        ammoCount.put(weaponType, current + amount);
    }
    
    /**
     * Get current weapon name.
     */
    public String getCurrentWeapon() {
        return currentWeapon;
    }
    
    /**
     * Get ammo for current weapon.
     */
    public int getCurrentAmmo() {
        return ammoCount.getOrDefault(currentWeapon, -1); // -1 means unlimited
    }
    
    /**
     * Load weapon definitions from config.
     */
    private void loadWeaponDefinitions() {
        // [TODO] Load from weapons_config.json
        // For now, hardcode defaults
        
        weapons.put("default_cannon", new WeaponDefinition(
            "Default Cannon",
            50,      // damage
            15.0f,   // projectile speed
            5.0f,    // fire rate (shots per second)
            3.0f,    // projectile lifetime
            true     // requires ammo
        ));
        
        weapons.put("energy_beam", new WeaponDefinition(
            "Energy Beam",
            75,      // damage
            25.0f,   // projectile speed
            3.0f,    // fire rate
            2.0f,    // projectile lifetime
            true     // requires ammo
        ));
        
        // Set initial ammo
        ammoCount.put("default_cannon", 100);
        ammoCount.put("energy_beam", 50);
    }
    
    // ========== Weapon Definition ==========
    
    public static class WeaponDefinition {
        public String name;
        public int damage;
        public float projectileSpeed;
        public float fireRate;        // Shots per second
        public float projectileLifetime;
        public boolean requiresAmmo;
        
        public WeaponDefinition(
                String name,
                int damage,
                float projectileSpeed,
                float fireRate,
                float projectileLifetime,
                boolean requiresAmmo) {
            this.name = name;
            this.damage = damage;
            this.projectileSpeed = projectileSpeed;
            this.fireRate = fireRate;
            this.projectileLifetime = projectileLifetime;
            this.requiresAmmo = requiresAmmo;
        }
    }
}
