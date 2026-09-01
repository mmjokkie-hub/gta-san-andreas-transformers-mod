# Architecture Documentation

## System Overview

The Bumblebee Transformer system is designed with **strict separation of concerns**. Each module has a single responsibility, well-defined inputs/outputs, and minimal cross-dependencies.

### Dependency Graph

```
┌──────────────────────────────────────────────┐
│      BumblebeeController (Main)         │  Orchestrates all subsystems
│  (Singleton managing the entire state)  │
└──────────────────────────┬──────────────┬──────────────────────────────┘
                   │
     ┌──────────────────┬──────────────────┬──────────────────────────┬─────────────────┐
     │             │             │                  │
     ▼             ▼             ▼                  ▼
┌────────────┐ ┌──────────────────────┐ ┌──────────┐      ┌────────────────────┐
│Transform  │ │RobotController       │ │Combat    │      │InputControl        │
│ Control   │ │                      │ │ Control    │      │
└─────┬──────┘ └────────────┬─────────┘ └─┬──────┘      └────────┬──────────┘
     │              │            │               │
     ▼              ▼            ▼               ▼
┌──────────┐ ┌──────────────┐ ┌────────┐      ┌──────────────┐
│Animation │ │  Movement    │ │ Weapons │      │ Input Maps  │
│Controller│ │ Physics      │ │& Damage │      │             │
└────┬─────┘ └──────────────┘ └───┬────┘      └──────────────┘
     │                            │
     └─────────────┬──────────────┘
                  ▼
        ┌──────────────────────┐
        │  AssetManager        │  Loads/unloads models, animations, sounds
        │  (Resource pooling)  │
        └──────────┬───────────┘
                  │
                  ▼
        ┌──────────────────────┐
        │   Config System      │  Centralized JSON-based settings
        │  (No hard-coding)    │
        └──────────┬───────────┘
                  │
                  ▼
        ┌──────────────────────┐
        │ Game Engine Bridge   │  Integration points for GTA SA APIs
        │  (Hooks/Injection)   │
        └──────────────────────┘
```

## Core Modules

### 1. BumblebeeController

**Role**: Main orchestrator. Single point of control for the entire Bumblebee system.

**Responsibilities**:
- Own the current state (VEHICLE, TRANSFORMING_TO_ROBOT, ROBOT, TRANSFORMING_TO_VEHICLE)
- Dispatch state changes to child controllers
- Coordinate transformation sequences
- Block conflicting inputs during transformations
- Manage lifecycle (init, update, cleanup)

**Interface**:
```java
public class BumblebeeController {
    public void initialize();
    public void update(float deltaTime);
    public void cleanup();
    
    public void beginTransformation();
    public void cancelTransformation();
    public BumblebeeState getCurrentState();
    
    public void onPlayerInput(InputAction action);
    public void onPlayerDamage(int amount, Vector3 source);
}
```

### 2. TransformationController

**Role**: Handles vehicle-to-robot and robot-to-vehicle transformations.

**Responsibilities**:
- Sequence transformation phases (unfolding, body repositioning, etc.)
- Play/sync animations across robot parts
- Manage physics/collision during transformation
- Prevent interruption and input during transformation
- Fire events on transformation completion

**Transformation Phases**:
1. PRE_TRANSFORM (disable inputs, stop movement)
2. ANIMATION_PLAY (play transformation sequence animation)
3. MODEL_SWAP (swap vehicle model for robot model when appropriate)
4. POST_TRANSFORM (enable inputs, restore physics)

**Interface**:
```java
public class TransformationController {
    public void startTransformation(TransformDirection direction);
    public void update(float deltaTime);
    public boolean isTransforming();
    public float getTransformationProgress(); // 0.0 to 1.0
}
```

### 3. RobotController

**Role**: Controls the robot in ROBOT state.

**Responsibilities**:
- Handle robot movement (walk, run, turn)
- Play movement animations (idle, walk cycle, run cycle)
- Handle robot rotation/facing direction
- Manage jumping and falling
- Apply gravity and ground collision
- Delegate melee attacks to CombatController
- Delegate weapon firing to WeaponController

**Interface**:
```java
public class RobotController {
    public void update(float deltaTime);
    public void setMovementInput(Vector3 direction, boolean running);
    public void setFacingDirection(float yaw);
    public void jump();
    public Vector3 getPosition();
    public float getHealth();
    public void takeDamage(int amount, Vector3 source);
}
```

### 4. CombatController

**Role**: Orchestrates combat (melee and ranged).

**Responsibilities**:
- Execute melee attacks with hit detection
- Manage weapon firing and ammo
- Handle damage calculation
- Apply hit reactions (stagger, knockback, etc.)
- Manage health and death state
- Fire explosion effects and sound

**Interface**:
```java
public class CombatController {
    public void meleeAttack();
    public void fireWeapon(Vector3 direction);
    public void takeDamage(int amount, Vector3 source);
    public void addAmmo(String weaponType, int amount);
    public int getHealth();
    public boolean isDead();
}
```

### 5. WeaponController

**Role**: Manages weapon definitions and firing logic.

**Responsibilities**:
- Load weapon configurations from JSON
- Handle weapon switching
- Create projectiles and apply weapon effects
- Manage ammo counts
- Apply weapon recoil/cooldown

**Interface**:
```java
public class WeaponController {
    public void switchWeapon(String weaponType);
    public void fire(Vector3 position, Vector3 direction);
    public String getCurrentWeapon();
    public int getAmmo();
    public void reload();
}
```

### 6. InputController

**Role**: Abstract input handling from the rest of the system.

**Responsibilities**:
- Read player input (gamepad, touch, keyboard)
- Map raw input to logical actions (TRANSFORM, ATTACK, AIM, etc.)
- Block inputs during transformation
- Fire input events to BumblebeeController and children
- Load input mapping from configuration

**Interface**:
```java
public class InputController {
    public void update();
    public boolean isActionPressed(InputAction action);
    public Vector2 getMovementInput();
    public void registerCallback(InputAction action, Callback callback);
}
```

### 7. AnimationController

**Role**: Plays and blends animations.

**Responsibilities**:
- Load animation clips from AssetManager
- Play, loop, blend animations
- Track animation playback time
- Notify when animations complete
- Handle multi-track animation blending (upper body vs lower body)

**Interface**:
```java
public class AnimationController {
    public void playAnimation(String clipName, boolean loop, float blendTime);
    public void stopAnimation(float blendTime);
    public boolean isAnimationPlaying(String clipName);
    public float getAnimationProgress();
    public void update(float deltaTime);
}
```

### 8. EffectsController

**Role**: Manages visual and audio effects.

**Responsibilities**:
- Spawn particles (dust, sparks, explosions)
- Play sound effects (footsteps, weapon fire, transformation)
- Manage screen shake on explosions
- Pool reusable effects for performance
- Respect quality settings for low-end devices

**Interface**:
```java
public class EffectsController {
    public void spawnExplosion(Vector3 position, float radius, int damage);
    public void playSound(String soundName, Vector3 position);
    public void createParticleEffect(String effectType, Vector3 position);
    public void update(float deltaTime);
}
```

### 9. AssetManager

**Role**: Centralized asset loading and pooling.

**Responsibilities**:
- Load models, textures, animations, sounds on demand
- Cache assets to avoid redundant loading
- Pool reusable objects (projectiles, particles)
- Unload unused assets
- Handle missing asset gracefully

**Interface**:
```java
public class AssetManager {
    public Model loadModel(String modelName);
    public AnimationClip loadAnimation(String animationName);
    public Sound loadSound(String soundName);
    public void unloadAsset(String assetName);
    public void preloadAssets(String[] assetNames);
}
```

### 10. Config System

**Role**: Centralize all configuration, avoid hard-coded values.

**Responsibilities**:
- Load JSON configuration files
- Provide typed config accessors (no string casting)
- Support runtime config updates
- Default fallback values for missing config

**Files**:
- `bumblebee_config.json` - Size, speed, health, state timings
- `robot_config.json` - Movement speed, rotation, jump height
- `weapons_config.json` - Weapon definitions, damage, fire rate, ammo
- `input_mapping.json` - Input action to button/key mapping
- `effects_config.json` - Particle counts, sound volumes, quality presets

**Interface**:
```java
public class Config {
    public static float getRobotSpeed();
    public static int getRobotHealth();
    public static WeaponDef getWeapon(String name);
    public static InputMapping getInputMapping();
}
```

## State Machine

### States and Transitions

```
┌──────────┐
│ VEHICLE  │ ◄──────────────────────────────────────────────────────┐
└──────┬───┘                                                        │
   │                                                            TRANSFORM
  TRANSFORM                                                      │
   │                                                            │
   ▼                                                            │
┌────────────────────────────────────────┐                      │
│ TRANSFORMING_TO_ROBOT                  │ ─────COMPLETE────────▼
└───────────────────────��────────────────┘            ┌──────────────────┐
   │                              │                    │             │
   └──────────────┬──────────────┘                     │             │
                  ▼                                     │             │
            ┌────────┐◄────────────────────────TRANSFORM────┐        │
            │ ROBOT  │                           │           │
            └────────┘                           │           │
                  ▲                            ┌──────────────────────┐
                  │                            │TRANSFORMING_TO_VEHICLE│
                  │                            └──────────────────────┘
```

### State Transition Rules

- **VEHICLE → TRANSFORMING_TO_ROBOT**: Player presses TRANSFORM button while in vehicle
- **TRANSFORMING_TO_ROBOT → ROBOT**: Transformation animation completes
- **ROBOT → TRANSFORMING_TO_VEHICLE**: Player presses TRANSFORM button while in robot
- **TRANSFORMING_TO_VEHICLE → VEHICLE**: Transformation animation completes
- **Any state → Any state**: Blocked if player is dead or in mid-damage reaction

### Guard Conditions

Transformation is blocked if:
- Currently transforming (mid-animation)
- Robot/vehicle is airborne
- Robot/vehicle is colliding with obstacles
- Player health <= 0
- Required assets not loaded

## Data Flow

### Update Loop (Every Frame)

```
1. InputController.update()
   ├─ Read raw input
   ├─ Map to InputActions
   └─ Fire callbacks

2. BumblebeeController.update(deltaTime)
   ├─ Check current state
   ├─ If VEHICLE:
   │  └─ Forward input to VehicleController
   ├─ If TRANSFORMING_TO_ROBOT or TRANSFORMING_TO_VEHICLE:
   │  └─ TransformationController.update()
   │     └─ Play animations, swap models
   ├─ If ROBOT:
   │  ├─ RobotController.update()
   │  │  ├─ Apply movement from input
   │  │  ├─ Play movement animations
   │  │  └─ Update physics
   │  ├─ CombatController.update()
   │  │  └─ Handle active attacks
   │  └─ WeaponController.update()
   │     └─ Manage weapon cooldowns
   └─ Handle state transitions

3. AnimationController.update(deltaTime)
   └─ Advance all playing animations

4. EffectsController.update(deltaTime)
   └─ Update particles, managed sounds

5. Render scene with current model/animation
```

### Transformation Sequence

```
Player presses TRANSFORM
   │
   ▼
BumblebeeController checks guards
   │
   ├─ Guards OK?
   │  ├─ Yes: Proceed
   │  └─ No: Abort, play "cannot transform" sound
   │
   ▼
TransformationController.startTransformation(TO_ROBOT)
   │
   ├─ Set state to TRANSFORMING_TO_ROBOT
   ├─ Freeze player input/movement
   ├─ Play transformation animation
   │  └─ Animation unfolds vehicle into robot parts
   ├─ At midpoint: Swap vehicle model for robot model
   ├─ Continue animation
   ├─ At end: Enable robot input, restore physics
   └─ Set state to ROBOT
   │
   ▼
Player can now move as robot
```

## Message/Event System

Systems communicate via events to maintain loose coupling:

```java
enum BumblebeeEvent {
    TRANSFORMATION_STARTED,
    TRANSFORMATION_COMPLETED,
    STATE_CHANGED,
    ROBOT_ATTACKED,
    ROBOT_HIT,
    ROBOT_DIED,
    WEAPON_FIRED,
    AMMO_DEPLETED,
    INPUT_RECEIVED
}

public interface EventListener {
    void onEvent(BumblebeeEvent event, Object data);
}
```

Example:
```java
// When damage is taken:
EventBus.fire(BumblebeeEvent.ROBOT_HIT, new {
    amount: 10,
    source: hitPosition
});

// EffectsController listens and spawns blood particle
```

## Performance Considerations

### Memory
- **Object Pooling**: Reuse projectiles, particles, effects
- **Asset Caching**: Load models once, reuse references
- **Lazy Loading**: Load robot assets only when first transformation starts
- **No Per-Frame Allocations**: Pre-allocate collections, reuse buffers

### CPU
- **Config Quality Levels**: Reduce particle count, effect complexity on low-end
- **Animation LOD**: Skip animation blending for distant objects
- **Spatial Queries**: Cache collision results, update only when needed
- **Projectile Pooling**: Limit max active projectiles

### GPU
- **Batching**: Group particle renders, use atlases
- **Effect Quality Settings**: Ultra/High/Medium/Low particle counts
- **Model LOD**: Simpler robot model for distance

## Error Handling

### Missing Assets
- If model fails to load: Log error, show placeholder, allow graceful degradation
- If animation missing: Use fallback animation (idle, stand)
- If config file missing: Use hardcoded defaults

### State Violations
- If transformation blocked by guard: Log reason, play error sound
- If invalid state transition: Log and revert to previous state
- If player input during transformation: Queue action for post-transform

## Testing Strategy

1. **Unit Tests**: State transitions, config loading, damage calculation
2. **Integration Tests**: Transformation sequence, weapon firing, animation playback
3. **Performance Tests**: Memory profiling, frame time on target devices
4. **Android Device Tests**: Test on low/mid-range Android phones
