package com.transformers.bumblebee.effects;

import com.transformers.bumblebee.utils.Vector3;
import java.util.*;

/**
 * Manages visual and audio effects.
 * 
 * Responsibilities:
 * - Play sound effects (with spatial audio)
 * - Spawn particle effects (dust, sparks, explosions)
 * - Manage screen shake
 * - Pool reusable effects for performance
 * - Respect quality settings
 */
public class EffectsController {
    
    private List<SoundEffect> activeSounds = new ArrayList<>();
    private List<ParticleEffect> activeParticles = new ArrayList<>();
    private Queue<ParticleEffect> particlePool = new LinkedList<>();
    
    // Quality settings (can be adjusted based on device performance)
    private int maxParticles = 100;
    private int maxSounds = 8;
    private float particleQuality = 1.0f; // 0.5 = low quality, 1.0 = full quality
    
    /**
     * Play a sound effect.
     */
    public void playSound(String soundName, Vector3 position) {
        if (activeSounds.size() >= maxSounds) {
            return; // Limit active sounds for performance
        }
        
        try {
            SoundEffect sound = new SoundEffect(soundName, position);
            sound.play();
            activeSounds.add(sound);
            
        } catch (Exception e) {
            System.err.println("[EffectsController] Failed to play sound " + soundName + ": " + e.getMessage());
        }
    }
    
    /**
     * Create a particle effect.
     */
    public void createParticleEffect(String effectName, Vector3 position) {
        if (activeParticles.size() >= maxParticles) {
            return; // Limit particles for performance
        }
        
        // Adjust particle count based on quality setting
        int particleCount = (int) (10 * particleQuality);
        
        ParticleEffect effect = new ParticleEffect(effectName, position, particleCount);
        activeParticles.add(effect);
    }
    
    /**
     * Spawn an explosion effect with damage.
     */
    public void spawnExplosion(Vector3 position, float radius, int damage) {
        // Visual effect
        createParticleEffect("explosion_burst", position);
        
        // Sound
        playSound("explosion_1", position);
        
        // Screen shake (if camera is close enough)
        // [TODO] Implement screen shake
        
        // [TODO] Calculate damage to nearby entities
        // Entities within radius take damage
    }
    
    /**
     * Update all active effects every frame.
     */
    public void update(float deltaTime) {
        // Update sounds
        List<SoundEffect> finishedSounds = new ArrayList<>();
        for (SoundEffect sound : activeSounds) {
            sound.update(deltaTime);
            if (sound.isFinished()) {
                finishedSounds.add(sound);
            }
        }
        activeSounds.removeAll(finishedSounds);
        
        // Update particles
        List<ParticleEffect> finishedParticles = new ArrayList<>();
        for (ParticleEffect particle : activeParticles) {
            particle.update(deltaTime);
            if (particle.isFinished()) {
                finishedParticles.add(particle);
            }
        }
        activeParticles.removeAll(finishedParticles);
    }
    
    /**
     * Set effect quality (for low-end devices).
     */
    public void setQuality(float quality) {
        this.particleQuality = Math.max(0.25f, Math.min(quality, 1.0f));
        this.maxParticles = (int) (100 * particleQuality);
        System.out.println("[EffectsController] Quality set to " + particleQuality);
    }
    
    /**
     * Clean up all effects.
     */
    public void cleanup() {
        activeSounds.clear();
        activeParticles.clear();
        particlePool.clear();
    }
    
    // ========== Effect Classes ==========
    
    public static class SoundEffect {
        public String name;
        public Vector3 position;
        public float duration = 3.0f; // Default duration
        public float elapsed = 0.0f;
        
        public SoundEffect(String name, Vector3 position) {
            this.name = name;
            this.position = position.copy();
        }
        
        public void play() {
            // [INTEGRATION POINT] Play sound via game engine
            System.out.println("[SoundEffect] Playing " + name + " at " + position);
        }
        
        public void update(float deltaTime) {
            elapsed += deltaTime;
        }
        
        public boolean isFinished() {
            return elapsed >= duration;
        }
    }
    
    public static class ParticleEffect {
        public String name;
        public Vector3 position;
        public int particleCount;
        public float duration = 2.0f;
        public float elapsed = 0.0f;
        public List<Particle> particles = new ArrayList<>();
        
        public ParticleEffect(String name, Vector3 position, int particleCount) {
            this.name = name;
            this.position = position.copy();
            this.particleCount = particleCount;
            
            // Initialize particles
            for (int i = 0; i < particleCount; i++) {
                particles.add(new Particle(position));
            }
        }
        
        public void update(float deltaTime) {
            elapsed += deltaTime;
            
            for (Particle p : particles) {
                p.update(deltaTime);
            }
        }
        
        public boolean isFinished() {
            return elapsed >= duration;
        }
    }
    
    public static class Particle {
        public Vector3 position;
        public Vector3 velocity;
        public float lifetime = 1.0f;
        public float age = 0.0f;
        
        public Particle(Vector3 origin) {
            this.position = origin.copy();
            // Random velocity in all directions
            this.velocity = new Vector3(
                (float) (Math.random() - 0.5f) * 5,
                (float) (Math.random() - 0.5f) * 5,
                (float) (Math.random() - 0.5f) * 5
            );
        }
        
        public void update(float deltaTime) {
            position = position.add(velocity.multiply(deltaTime));
            age += deltaTime;
        }
    }
}
