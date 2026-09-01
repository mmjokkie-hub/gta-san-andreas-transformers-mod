package com.transformers.bumblebee.animation;

import com.transformers.bumblebee.assets.AssetManager;
import java.util.*;

/**
 * Manages animation playback and blending.
 * 
 * Responsibilities:
 * - Play animation clips
 * - Blend between animations
 * - Track playback progress
 * - Notify when animations complete
 */
public class AnimationController {
    
    private final AssetManager assetManager;
    private Map<String, AnimationClip> playingAnimations = new HashMap<>();
    private Queue<AnimationClip> animationQueue = new LinkedList<>();
    
    public AnimationController(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    /**
     * Play an animation.
     * 
     * @param clipName Name of animation to play
     * @param loop Whether to loop
     * @param blendTime Transition duration
     */
    public void playAnimation(String clipName, boolean loop, float blendTime) {
        try {
            // Load animation asset
            AnimationClip clip = assetManager.loadAnimationClip(clipName);
            if (clip == null) {
                System.err.println("[AnimationController] Animation not found: " + clipName);
                return;
            }
            
            clip.loop = loop;
            clip.blendTime = blendTime;
            clip.elapsed = 0.0f;
            
            // Stop current animation and start new one
            playingAnimations.clear();
            playingAnimations.put(clipName, clip);
            
            System.out.println("[AnimationController] Playing " + clipName + (loop ? " (looping)" : ""));
            
        } catch (Exception e) {
            System.err.println("[AnimationController] Failed to play animation: " + e.getMessage());
        }
    }
    
    /**
     * Stop animation with blend-out time.
     */
    public void stopAnimation(float blendTime) {
        // [TODO] Implement fade-out over blendTime
        playingAnimations.clear();
    }
    
    /**
     * Check if a specific animation is playing.
     */
    public boolean isAnimationPlaying(String clipName) {
        return playingAnimations.containsKey(clipName);
    }
    
    /**
     * Get progress of current animation (0.0 to 1.0).
     */
    public float getAnimationProgress() {
        if (playingAnimations.isEmpty()) {
            return 0.0f;
        }
        
        AnimationClip clip = playingAnimations.values().iterator().next();
        return Math.min(clip.elapsed / clip.duration, 1.0f);
    }
    
    /**
     * Update all playing animations.
     */
    public void update(float deltaTime) {
        List<String> finished = new ArrayList<>();
        
        for (Map.Entry<String, AnimationClip> entry : playingAnimations.entrySet()) {
            AnimationClip clip = entry.getValue();
            clip.elapsed += deltaTime;
            
            // Check if animation finished
            if (clip.elapsed >= clip.duration) {
                if (clip.loop) {
                    clip.elapsed = clip.elapsed % clip.duration; // Loop
                } else {
                    finished.add(entry.getKey());
                }
            }
        }
        
        // Remove finished animations
        for (String clipName : finished) {
            playingAnimations.remove(clipName);
        }
    }
    
    public void cleanup() {
        playingAnimations.clear();
    }
    
    // ========== Animation Clip Class ==========
    
    public static class AnimationClip {
        public String name;
        public float duration;  // In seconds
        public float elapsed;
        public boolean loop;
        public float blendTime; // Transition duration
        
        public AnimationClip(String name, float duration) {
            this.name = name;
            this.duration = duration;
            this.elapsed = 0.0f;
            this.loop = false;
            this.blendTime = 0.2f;
        }
    }
}
