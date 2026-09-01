package com.transformers.bumblebee.combat;

import com.transformers.bumblebee.animation.AnimationController;
import com.transformers.bumblebee.effects.EffectsController;
import com.transformers.bumblebee.robot.RobotController;
import com.transformers.bumblebee.weapons.WeaponController;
import com.transformers.bumblebee.utils.Vector3;
import com.transformers.bumblebee.events.EventBus;
import com.transformers.bumblebee.events.BumblebeeEvent;

/**
 * Orchestrates combat actions (melee and ranged).
 * 
 * Handles:
 * - Melee attack execution and hit detection
 * - Weapon firing
 * - Damage calculation and application
 * - Hit reactions (stagger, knockback, animations)
 * - Death state management
 */
public class CombatController {
    
    private final RobotController robotController;
    private final AnimationController animationController;
    private final EffectsController effectsController;
    private final WeaponController weaponController;
    
    // Combat state
    private MeleeAttack currentMeleeAttack;
    private float attackCooldownTimer = 0.0f;
    private static final float MELEE_COOLDOWN = 0.5f; // 500ms between attacks
    
    public CombatController(
            RobotController robotController,
            AnimationController animationController,
            EffectsController effectsController) {
        this.robotController = robotController;
        this.animationController = animationController;
        this.effectsController = effectsController;
        this.weaponController = new WeaponController();
    }
    
    /**
     * Update combat state every frame.
     */
    public void update(float deltaTime) {
        // Reduce cooldown timer
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= deltaTime;
        }
    }
    
    /**
     * Execute a melee attack.
     */
    public void meleeAttack() {
        if (attackCooldownTimer > 0 || robotController.isDead()) {
            return; // On cooldown or dead
        }
        
        // Play melee animation
        animationController.playAnimation("melee_punch_1", false, 0.1f);
        
        // Create melee attack object
        currentMeleeAttack = new MeleeAttack(
            robotController.getPosition(),
            robotController.getYaw(),
            3.0f,  // Reach: 3 units
            30     // Damage: 30 HP
        );
        
        // Play attack sound
        effectsController.playSound("attack_punch", robotController.getPosition());
        
        // Reset cooldown
        attackCooldownTimer = MELEE_COOLDOWN;
        
        // Fire event
        EventBus.fire(BumblebeeEvent.ROBOT_ATTACKED, "melee");
        
        // [TODO] Raycast for hit detection
        // performMeleeHitDetection();
    }
    
    /**
     * Fire weapon.
     */
    public void fireWeapon(Vector3 targetDirection) {
        if (robotController.isDead()) {
            return;
        }
        
        weaponController.fireWeapon(
            robotController.getPosition(),
            targetDirection
        );
        
        // Play firing animation
        animationController.playAnimation("attack_energy_beam", false, 0.2f);
        
        // Play firing sound
        String currentWeapon = weaponController.getCurrentWeapon();
        effectsController.playSound("weapon_" + currentWeapon, robotController.getPosition());
    }
    
    /**
     * Apply damage to robot.
     */
    public void takeDamage(int amount, Vector3 source) {
        robotController.takeDamage(amount, source);
    }
    
    /**
     * Add ammo for a weapon type.
     */
    public void addAmmo(String weaponType, int amount) {
        weaponController.addAmmo(weaponType, amount);
    }
    
    /**
     * Switch to a different weapon.
     */
    public void switchWeapon(String weaponType) {
        weaponController.switchWeapon(weaponType);
    }
    
    /**
     * Get current health.
     */
    public int getHealth() {
        return robotController.getHealth();
    }
    
    /**
     * Check if robot is dead.
     */
    public boolean isDead() {
        return robotController.isDead();
    }
    
    /**
     * Get current weapon.
     */
    public String getCurrentWeapon() {
        return weaponController.getCurrentWeapon();
    }
    
    /**
     * Get ammo for current weapon.
     */
    public int getCurrentAmmo() {
        return weaponController.getCurrentAmmo();
    }
    
    // ========== Melee Attack Class ==========
    
    public static class MeleeAttack {
        public Vector3 position;      // Where the attack originates
        public float direction;       // Facing direction (yaw)
        public float reach;           // How far the attack extends
        public int damage;            // Damage on hit
        public float lifetime = 0.5f; // How long attack is active
        
        public MeleeAttack(Vector3 position, float direction, float reach, int damage) {
            this.position = position;
            this.direction = direction;
            this.reach = reach;
            this.damage = damage;
        }
        
        /**
         * Check if this attack can hit a target at the given position.
         */
        public boolean canHit(Vector3 targetPos) {
            Vector3 delta = targetPos.subtract(position);
            float distance = delta.length();
            
            // Check if target is within reach
            if (distance > reach) {
                return false;
            }
            
            // [TODO] Check if target is in front of attacker (cone check)
            return true;
        }
    }
}
