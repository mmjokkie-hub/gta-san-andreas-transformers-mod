# GTA San Andreas Transformers Mod

A modular Transformers mod system for GTA San Andreas Android. The Cheetah vehicle transforms into a large Bumblebee-style robot inspired by live-action Transformers movies.

## Vision

**Phase 1 (Current):** Bumblebee transformation system foundation
- Cheetah → Bumblebee vehicle-to-robot transformation
- State machine and animation foundation
- Input layer and basic movement
- Combat and weapon systems framework

**Phase 2+:** Additional Transformers (Optimus Prime, etc.)
- Modular architecture supports adding new Transformers
- Vehicle → Robot transformation pipeline reusable for all characters

## Architecture Overview

The system is built with **clean separation of concerns** using modular controllers:

```
BumblebeeController (Main orchestrator)
├── TransformationController (State machine, animation sequencing)
├── RobotController (Movement, animations, physics)
├── VehicleController (Vehicle mode handling)
├── CombatController (Attack logic, damage)
├── WeaponController (Weapon definitions, firing)
├── InputController (Touch/gamepad input mapping)
├── AnimationController (Playback, blending)
├── EffectsController (Visuals, particles, explosions)
├── AssetManager (Models, textures, sounds, animations)
└── Config (Centralized settings, no hard-coding)
```

## Key Design Principles

1. **Modularity**: Each system is independent; dependencies flow downward
2. **No Game Executable Modification**: Uses hooks/injection only
3. **Android Performance**: Minimal per-frame allocation, configurable quality
4. **Asset Flexibility**: Models, animations, textures can be swapped without code rewrites
5. **Extensibility**: Adding a new Transformer reuses the entire architecture

## Bumblebee States

```
VEHICLE
  ↓ (Transform)
TRANSFORMING_TO_ROBOT (Animation playing, no input)
  ↓ (Complete)
ROBOT
  ↓ (Transform)
TRANSFORMING_TO_VEHICLE (Animation playing, no input)
  ↓ (Complete)
VEHICLE
```

**State Transition Rules:**
- Transformations cannot be interrupted
- No movement/combat during transformation
- Damage, obstacles, and collisions are blocked during transformation

## Directory Structure

```
/
├── README.md (this file)
├── ARCHITECTURE.md (Detailed system design)
├── INTEGRATION_POINTS.md (Game engine hooks, unknown APIs)
├── docs/
│   ├── STATE_MACHINE.md
│   ├── TRANSFORMATION_PIPELINE.md
│   ├── COMBAT_SYSTEM.md
│   └── ASSET_LOADING.md
├── src/
│   ├── core/
│   │   ├── BumblebeeController.java
│   │   ├── BumblebeeState.java
│   │   └── Transformer.java
│   ├── transformation/
│   │   ├── TransformationController.java
│   │   ├── TransformationSequence.java
│   │   └── TransformationPhase.java
│   ├── robot/
│   │   ├── RobotController.java
│   │   ├── RobotMovement.java
│   │   └── RobotAnimation.java
│   ├── vehicle/
│   │   ├── VehicleController.java
│   │   └── VehicleState.java
│   ├── combat/
│   │   ├── CombatController.java
│   │   ├── CombatAction.java
│   │   └── DamageCalculator.java
│   ├── weapons/
│   │   ├── WeaponController.java
│   │   ├── WeaponDefinition.java
│   │   ├── Projectile.java
│   │   └── ProjectileEffect.java
│   ├── input/
│   │   ├── InputController.java
│   │   ├── InputAction.java
│   │   └── InputMapping.java
│   ├── animation/
│   │   ├── AnimationController.java
│   │   ├── AnimationClip.java
│   │   └── AnimationBlender.java
│   ├── effects/
│   │   ├── EffectsController.java
│   │   ├── ParticleEffect.java
│   │   ├── ExplosionEffect.java
│   │   └── SoundEffect.java
│   ├── assets/
│   │   ├── AssetManager.java
│   │   ├── ModelAsset.java
│   │   ├── AnimationAsset.java
│   │   └── SoundAsset.java
│   ├── config/
│   │   ├── Config.java
│   │   ├── BumblebeeConfig.java
│   │   ├── TransformationConfig.java
│   │   ├── RobotConfig.java
│   │   ├── CombatConfig.java
│   │   └── WeaponConfig.java
│   └── utils/
│       ├── Vector3.java
│       ├── Quaternion.java
│       ├── Timer.java
│       └── PooledObject.java
├── assets/
│   ├── models/
│   │   ├── bumblebee_robot.dff
│   │   └── cheetah_vehicle.dff
│   ├── textures/
│   │   ├── bumblebee_*.txd
│   │   └── cheetah_*.txd
│   ├── animations/
│   │   ├── transform_to_robot.ifp
│   │   ├── transform_to_vehicle.ifp
│   │   ├── walk.ifp
│   │   ├── run.ifp
│   │   ├── attack_melee.ifp
│   │   └── ...
│   ├── sounds/
│   │   ├── transform.wav
│   │   ├── footstep_*.wav
│   │   ├── attack_*.wav
│   │   └── ...
│   └── config/
│       ├── bumblebee_config.json
│       ├── weapons_config.json
│       └── input_mapping.json
├── tests/
│   ├── StateTransitionTests.java
│   ├── TransformationTests.java
│   └── CombatTests.java
└── build.gradle (or Maven pom.xml)
```

## Building & Testing

```bash
# Compile
./gradlew build

# Deploy to Android device
adb install build/outputs/apk/release/mod.apk

# Test transformation
# Trigger via in-game menu or command
```

## Controls (Configurable)

| Action | Default | Type |
|--------|---------|------|
| Transform | SPACE (or touch button) | Toggle |
| Attack | LEFT_CLICK (or touch) | Press |
| Aim | RIGHT_CLICK (or touch) | Hold |
| Weapon | W (or scroll) | Toggle |
| Move | WASD (or d-pad) | Analog |
| Run | SHIFT (or button) | Hold |
| Jump | SPACEBAR (or button) | Press |

All mappings are loaded from `assets/config/input_mapping.json` and can be changed without code recompilation.

## Performance Targets

- **Android low/mid-range support**: Minimal allocations per frame
- **Transformation**: 60 FPS animation playback
- **Robot movement**: 60 FPS with physics
- **Combat**: Real-time damage calculation, projectile pooling
- **Memory**: Configurable effect quality to reduce draw calls

## Integration Points

See `INTEGRATION_POINTS.md` for:
- Game engine hooks (unknown APIs marked clearly)
- Vehicle spawning/despawning
- Ped (pedestrian) model loading and animation
- Collision/physics integration
- Camera control
- HUD/UI integration

## Contributing

1. Keep systems modular
2. No hard-coded values (use Config classes)
3. All game engine APIs go through defined integration points
4. Test state transitions and transformation sequences
5. Profile memory/CPU on target Android device

## License

[License TBD]
