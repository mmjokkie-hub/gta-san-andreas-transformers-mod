# Game Engine Integration Points

This document defines all the GTA San Andreas Android APIs and hooks that the Bumblebee system requires.

> **Note**: Some APIs are currently unknown or not yet researched. These are marked with `[UNKNOWN]`.
> When implementing, replace placeholder method names with actual engine APIs.

## Game Engine Bridge

All game engine calls go through a single abstraction layer to isolate the mod from the engine:

```java
public class GameEngineBridge {
    // Vehicle APIs
    public static native Vehicle getPlayerVehicle();
    public static native void destroyVehicle(Vehicle vehicle);
    public static native void setVehicleModel(Vehicle vehicle, int modelId);
    
    // Ped APIs
    public static native Ped createPed(int modelId, Vector3 position);
    public static native void destroyPed(Ped ped);
    public static native void setPedModel(Ped ped, int modelId);
    public static native void setPedPosition(Ped ped, Vector3 position);
    public static native Vector3 getPedPosition(Ped ped);
    public static native void setPedRotation(Ped ped, float yaw);
    
    // Animation APIs
    public static native void playAnimation(Ped ped, String animLib, String animName, float speed, boolean loop);
    public static native void stopAnimation(Ped ped);
    public static native boolean isAnimationPlaying(Ped ped, String animLib, String animName);
    
    // Physics APIs
    public static native void applyForce(Ped ped, Vector3 force);
    public static native Vector3 getPedVelocity(Ped ped);
    public static native void setPedVelocity(Ped ped, Vector3 velocity);
    public static native boolean isPedOnGround(Ped ped);
    
    // Collision APIs
    public static native RaycastResult raycast(Vector3 from, Vector3 to, int flags);
    public static native Object getCollidingObject(Ped ped);
    public static native void setCollisionGroup(Ped ped, int group);
    
    // Camera APIs
    public static native void setCameraTarget(Vector3 position);
    public static native void setCameraFocus(Object entity);
    public static native Vector3 getCameraPosition();
    
    // Input APIs
    public static native boolean isKeyPressed(int keyCode);
    public static native Vector2 getAnalogInput(int axisId);
    public static native void vibrate(int durationMs);
    
    // Model/Asset APIs
    public static native int requestModel(String modelName);
    public static native void loadModel(int modelId);
    public static native boolean isModelLoaded(int modelId);
    public static native void unloadModel(int modelId);
    public static native int getModelDimensions(int modelId, Vector3 outDims);
    
    // Animation Asset APIs
    public static native boolean loadAnimLibrary(String libName);
    public static native void unloadAnimLibrary(String libName);
    public static native boolean isAnimLibraryLoaded(String libName);
    
    // Sound APIs
    public static native int loadSound(String soundPath);
    public static native void playSound(int soundId, Vector3 position, float volume);
    public static native void stopSound(int soundId);
    public static native void unloadSound(int soundId);
    
    // Weapon/Damage APIs
    public static native void givePedWeapon(Ped ped, int weaponId, int ammo);
    public static native void removePedWeapon(Ped ped, int weaponId);
    public static native void applyDamage(Ped ped, int amount, int damageType);
    public static native void fireWeapon(Ped ped, Vector3 targetPos, int rounds);
    
    // HUD/UI APIs [UNKNOWN]
    public static native void drawText(String text, float x, float y, int color);
    public static native void drawRectangle(float x, float y, float w, float h, int color);
    
    // Misc APIs
    public static native void playParticleEffect(String effectName, Vector3 position);
    public static native void createExplosion(Vector3 position, int type, float radius);
    public static native void screenFade(float duration, int color);
}
```

## Asset Loading

### Vehicle Models

**File Format**: `.dff` (binary GTA SA 3D model)

**Integration**:
```java
int cheetahModelId = GameEngineBridge.requestModel("cheetah");
GameEngineBridge.loadModel(cheetahModelId);
// Now model is ready to use
```

**Known Issue**: We do NOT assume the Cheetah model includes a robot form. The vehicle and robot are **separate models** with separate assets.

### Robot Models

The robot model (Bumblebee) is a **custom .dff file** that will be provided by the asset team.

**Characteristics**:
- Rigged for humanoid animation (arms, legs, torso, head)
- Substantially larger than normal GTA pedestrians
- Supports idle, walk, run, attack, and damage animations

**Integration**:
```java
int bumblebeeModelId = GameEngineBridge.requestModel("bumblebee_robot");
GameEngineBridge.loadModel(bumblebeeModelId);
Ped robotPed = GameEngineBridge.createPed(bumblebeeModelId, spawnPos);
```

### Animations

**File Format**: `.ifp` (GTA SA animation library)

**Required Animations**:
```
// Transformation
transform_to_robot.ifp
transform_to_vehicle.ifp

// Robot Movement
walk.ifp
run.ifp
idle.ifp
turn_left.ifp
turn_right.ifp
jump.ifp
land.ifp

// Combat
melee_punch_1.ifp
melee_punch_2.ifp
melee_kick.ifp
attack_heavy.ifp
attack_energy_beam.ifp

// Reactions
hit_front.ifp
hit_back.ifp
stagger.ifp
knockdown.ifp
death.ifp
```

**Integration**:
```java
// Load animation library
boolean loaded = GameEngineBridge.loadAnimLibrary("bumblebee_anims");

// Play animation
GameEngineBridge.playAnimation(robotPed, "bumblebee_anims", "walk", 1.0f, true);

// Check if playing
boolean isPlaying = GameEngineBridge.isAnimationPlaying(robotPed, "bumblebee_anims", "walk");
```

### Textures

**File Format**: `.txd` (GTA SA texture dictionary)

**Integration**:
```java
// Textures are typically loaded automatically when models are loaded
// If separate texture management needed:
GameEngineBridge.loadTexture("bumblebee_textures");
```

### Sounds

**File Format**: `.wav` or `.mp3`

**Required Sounds**:
```
transform_start.wav (mechanical unfolding)
transform_complete.wav (transformation finished)
footstep_heavy_1.wav
footstep_heavy_2.wav
footstep_heavy_3.wav
attack_punch.wav
attack_energy_beam.wav
explosion_1.wav
explosion_2.wav
hit_metallic.wav
death_explode.wav
warning_low_health.wav
```

**Integration**:
```java
int soundId = GameEngineBridge.loadSound("assets/sounds/transform_start.wav");
GameEngineBridge.playSound(soundId, robotPosition, 1.0f);
```

## Vehicle Handling

### Get Player Vehicle

```java
Vehicle playerVehicle = GameEngineBridge.getPlayerVehicle();
if (playerVehicle.getModelId() == CHEETAH_MODEL_ID) {
    // Player is in a Cheetah, can transform
}
```

### Despawn Vehicle, Spawn Robot

**Challenge**: When transforming from vehicle to robot, the vehicle must be despawned (or hidden) and a robot ped must be spawned at the same location.

```java
// Store vehicle state
Vector3 vehiclePos = playerVehicle.getPosition();
float vehicleHeading = playerVehicle.getHeading();

// Despawn vehicle
GameEngineBridge.destroyVehicle(playerVehicle);

// Create robot ped at same location
Ped robotPed = GameEngineBridge.createPed(BUMBLEBEE_MODEL_ID, vehiclePos);
GameEngineBridge.setPedRotation(robotPed, vehicleHeading);

// Make robot the player-controlled character
GameEngineBridge.setPlayerCharacter(robotPed);
```

**[UNKNOWN]**: The actual API to set the player's controllable character. May be:
- `GameEngineBridge.setPlayerCharacter(Ped)`
- `GameEngineBridge.changeCharacter(Ped)`
- Direct modification of player pointer

### Respawn Vehicle from Robot

```java
// Store robot state
Vector3 robotPos = robotPed.getPosition();
float robotHeading = robotPed.getRotation();

// Despawn robot
GameEngineBridge.destroyPed(robotPed);

// Create vehicle at same location
Vehicle cheetah = GameEngineBridge.createVehicle(CHEETAH_MODEL_ID, robotPos);
GameEngineBridge.setVehicleHeading(cheetah, robotHeading);

// Make vehicle the player-controlled vehicle
GameEngineBridge.warpPlayerIntoVehicle(cheetah);
```

## Physics & Movement

### Gravity & Ground Detection

```java
public void applyGravity(Ped ped, float deltaTime) {
    Vector3 velocity = GameEngineBridge.getPedVelocity(ped);
    
    if (!GameEngineBridge.isPedOnGround(ped)) {
        // Apply gravity
        velocity.y -= GRAVITY * deltaTime;
    } else {
        // Reset fall velocity when on ground
        velocity.y = 0;
    }
    
    GameEngineBridge.setPedVelocity(ped, velocity);
}
```

**[UNKNOWN]**: Exact gravity constant for GTA SA Android. Likely around 9.8 or scaled equivalent.

### Raycast for Ground Detection

```java
public boolean isGrounded(Ped ped) {
    Vector3 pedPos = GameEngineBridge.getPedPosition(ped);
    Vector3 below = pedPos.subtract(new Vector3(0, 2, 0)); // 2 units down
    
    RaycastResult result = GameEngineBridge.raycast(pedPos, below, RAYCAST_FLAG_BUILDINGS | RAYCAST_FLAG_VEHICLES);
    return result.hit;
}
```

### Movement

```java
public void moveRobot(Ped robotPed, Vector3 direction, float speed, float deltaTime) {
    // Get current velocity
    Vector3 velocity = GameEngineBridge.getPedVelocity(robotPed);
    
    // Apply movement in horizontal plane
    Vector3 moveVector = direction.normalized().multiply(speed * deltaTime);
    velocity.x += moveVector.x;
    velocity.z += moveVector.z;
    // Note: Don't touch velocity.y (gravity handles vertical)
    
    GameEngineBridge.setPedVelocity(robotPed, velocity);
}
```

## Collision & Obstacles

### Check for Collision

```java
public boolean canTransform(Ped ped) {
    // Check if ped is colliding with anything
    Object collidingObject = GameEngineBridge.getCollidingObject(ped);
    if (collidingObject != null) {
        return false; // Blocked by collision
    }
    
    // Check if on ground
    if (!GameEngineBridge.isPedOnGround(ped)) {
        return false; // Cannot transform mid-air
    }
    
    return true;
}
```

### Hit Detection for Melee Attacks

**Challenge**: GTA SA doesn't provide direct melee hit detection. We must implement raycast-based detection.

```java
public void executeMeleeAttack(Ped attacker, String attackType) {
    Vector3 attackerPos = GameEngineBridge.getPedPosition(attacker);
    float attackerYaw = GameEngineBridge.getPedRotation(attacker);
    
    // Calculate attack reach (in front of attacker)
    float reach = MELEE_REACH; // e.g., 3 units
    Vector3 attackDir = getForwardVector(attackerYaw);
    Vector3 targetPos = attackerPos.add(attackDir.multiply(reach));
    
    // Raycast to find what we hit
    RaycastResult result = GameEngineBridge.raycast(attackerPos, targetPos, RAYCAST_FLAG_PEDS);
    
    if (result.hit && result.hitPed != null) {
        Ped victim = result.hitPed;
        int damage = MELEE_DAMAGE_MAP.get(attackType);
        GameEngineBridge.applyDamage(victim, damage, DAMAGE_TYPE_MELEE);
    }
}
```

## Combat & Damage

### Apply Damage to Robot

```java
public void robotTakeDamage(Ped robotPed, int amount, Vector3 source) {
    // Apply damage
    GameEngineBridge.applyDamage(robotPed, amount, DAMAGE_TYPE_BULLET);
    
    // Play hit animation/sound
    playHitReaction(robotPed, source);
    
    // Check if dead
    if (robotPed.getHealth() <= 0) {
        onRobotDeath(robotPed, source);
    }
}
```

### Weapon Firing

```java
public void fireWeapon(Ped ped, int weaponId, Vector3 targetPos) {
    // Note: GTA SA's native weapon system may not support custom behavior.
    // We may need to implement projectile firing ourselves.
    
    // Option 1: Use native GTA weapon (if it exists)
    GameEngineBridge.fireWeapon(ped, targetPos, 1);
    
    // Option 2: Create custom projectile (likely needed for energy weapons)
    createProjectile(weaponId, ped.getPosition(), targetPos);
}
```

**[UNKNOWN]**: Whether GTA SA Android has native weapon firing APIs or if we must implement projectiles from scratch.

### Projectile System

If custom projectiles are needed:

```java
public class Projectile {
    private Vector3 position;
    private Vector3 velocity;
    private float lifetime;
    private int weaponId;
    
    public void update(float deltaTime) {
        // Move projectile
        position = position.add(velocity.multiply(deltaTime));
        lifetime -= deltaTime;
        
        // Check collision
        RaycastResult result = GameEngineBridge.raycast(position, position.add(velocity));
        if (result.hit) {
            onProjectileHit(result);
        }
        
        // Check lifetime
        if (lifetime <= 0) {
            onProjectileExpired();
        }
    }
}
```

## Camera Control

### Lock Camera to Robot

```java
public void updateCamera(Ped robotPed, float deltaTime) {
    Vector3 robotPos = GameEngineBridge.getPedPosition(robotPed);
    
    // Camera behind and above robot
    Vector3 cameraOffset = new Vector3(0, 2.5f, -5.0f);
    Vector3 cameraPos = robotPos.add(cameraOffset);
    
    GameEngineBridge.setCameraTarget(cameraPos);
    GameEngineBridge.setCameraFocus(robotPed);
}
```

**[UNKNOWN]**: Exact camera control APIs for GTA SA Android.

## Input Handling

### Read Touch Input

```java
public void updateInput() {
    // Read touch positions
    int touchCount = getTouchPointCount();
    for (int i = 0; i < touchCount; i++) {
        Vector2 touchPos = getTouchPosition(i);
        
        // Map to UI buttons or actions
        InputAction action = mapTouchToAction(touchPos);
        if (action != null) {
            inputController.fireInputAction(action);
        }
    }
}
```

### Read Gamepad Input

```java
public void readGamepadInput() {
    // Left stick = movement
    Vector2 leftStick = GameEngineBridge.getAnalogInput(GAMEPAD_AXIS_LX, GAMEPAD_AXIS_LY);
    
    // Right stick = look/aim
    Vector2 rightStick = GameEngineBridge.getAnalogInput(GAMEPAD_AXIS_RX, GAMEPAD_AXIS_RY);
    
    // Buttons
    boolean firePressed = GameEngineBridge.isKeyPressed(GAMEPAD_BUTTON_A);
    boolean aimPressed = GameEngineBridge.isKeyPressed(GAMEPAD_BUTTON_LT);
    boolean transformPressed = GameEngineBridge.isKeyPressed(GAMEPAD_BUTTON_Y);
}
```

## Visual Effects

### Particle Effects

```java
public void createExplosionEffect(Vector3 position) {
    // Native GTA SA effect
    GameEngineBridge.playParticleEffect("explosion_large", position);
    
    // Or custom particle system
    EffectsController.spawnExplosion(position, 10.0f, 50);
}
```

### Screen Shake

```java
public void createScreenShake(Vector3 epicenter, float radius, float duration) {
    Vector3 cameraPos = GameEngineBridge.getCameraPosition();
    float distance = cameraPos.distance(epicenter);
    
    if (distance < radius) {
        float intensity = (radius - distance) / radius; // 0 to 1
        GameEngineBridge.screenShake(duration, intensity);
    }
}
```

## HUD/UI Integration

### Draw Health Bar

```java
public void renderHealthBar(Ped robotPed, float screenX, float screenY) {
    int health = robotPed.getHealth();
    int maxHealth = 200;
    float healthPercent = (float) health / maxHealth;
    
    // Draw background
    GameEngineBridge.drawRectangle(screenX, screenY, 100, 10, COLOR_BLACK);
    
    // Draw health bar
    GameEngineBridge.drawRectangle(screenX, screenY, 100 * healthPercent, 10, COLOR_GREEN);
    
    // Draw text
    GameEngineBridge.drawText("Health: " + health, screenX, screenY - 15, COLOR_WHITE);
}
```

**[UNKNOWN]**: Whether GTA SA Android provides native HUD drawing or if we must render to a texture/canvas.

## Summary of [UNKNOWN] APIs

1. **Player Character Switch**: How to change which ped the player controls
2. **Camera APIs**: Exact functions for camera control
3. **HUD Drawing**: How to draw text/rectangles on screen
4. **Weapon Firing**: Native weapon APIs or must implement from scratch
5. **Touch Input**: Exact touch API available on Android
6. **Gamepad Mapping**: How to read gamepad axes/buttons
7. **Screen Shake**: Exact API signature
8. **Gravity Constant**: Correct value for GTA SA physics

**Next Step**: Research GTA SA modding forums and ASI/Cleo documentation to identify actual APIs.
