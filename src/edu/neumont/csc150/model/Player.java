package edu.neumont.csc150.model;

import edu.neumont.csc150.controller.Map;
import edu.neumont.csc150.model.colliders.BoxCollider;
import edu.neumont.csc150.model.colliders.CollisionEvent;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.colliders.SphereCollider;
import edu.neumont.csc150.model.enums.*;
import edu.neumont.csc150.model.misc.AudioPlayer;
import edu.neumont.csc150.model.misc.Camera;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.model.misc.raycast.HitInfo;
import edu.neumont.csc150.model.projectiles.FireBolt;
import edu.neumont.csc150.service.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Player implements CollisionEvent {
    private enum HandState {
        IDLE,
        LEFT_FIRE,
        RIGHT_FIRE,
        MUFFIN_IDLE,
        MUFFIN_FIRE
    }

    private final float SPEED = 3;
    private final float ROT_SPEED = 50;
    private final float MOUSE_SENSITIVITY = 0.5f;
    private final float JUMP_SPEED = 8;
    private final float JUMP_HEIGHT = 0.85f;
    private final float FALL_SPEED = 1.5f;
    private final float MAX_HEALTH = 5;

    private final UserInput inputService;
    private final ConfigService configService;
    private final CollisionService collisionService;
    private final SceneService sceneService;
    private final SoundService soundService;
    private final Camera camera;
    private final Random rand = new Random();

    private Vector3 position = Vector3.zero();
    private Vector3 velocity = Vector3.zero();
    private volatile boolean isJumping = false;
    private volatile boolean invincible = false;
    public float rotation;
    private SphereCollider collider;
    private float health = MAX_HEALTH;
    private volatile HandState handState = HandState.IDLE;
    private volatile HandState lastHandFireState = HandState.IDLE;
    private volatile boolean holdingMuffin = false;
    private Timer handThread;
    private volatile boolean canCast = true;

    private final AudioPlayer fireBoltSound;
    private final AudioPlayer eatSound;
    private final AudioPlayer hurtSound1;
    private final AudioPlayer hurtSound2;

    public Player(
            Injectable userInput,
            Injectable config,
            Injectable collision,
            Camera camera,
            Injectable scene,
            Injectable sound
    ) {
        inputService = (UserInput) userInput;
        configService = (ConfigService) config;
        collisionService = (CollisionService) collision;
        sceneService = (SceneService) scene;
        soundService = (SoundService) sound;

        this.camera = camera;
        collider = new SphereCollider(
                this,
                collisionService,
                new RenderLayer[]{RenderLayer.COLLISION_ONLY},
                new CollisionLayer[]{
                        CollisionLayer.ENEMIES,
                        CollisionLayer.ENVIRONMENT,
                        CollisionLayer.PROJECTILE_ENEMY,
                        CollisionLayer.MUFFIN
                },
                new CollisionLayer[]{CollisionLayer.PLAYER}
        );
        collider.setRadius(0.75f);

        fireBoltSound = soundService.create(
                "assets/audio/sound effects/Firebolt.wav",
                false
        );
        fireBoltSound.setVolume(0.9f);

        eatSound = soundService.create(
                "assets/audio/sound effects/Eat Muffin.wav",
                false
        );

        hurtSound1 = soundService.create(
                "assets/audio/sound effects/Hurt 1.wav",
                false
        );
        hurtSound1.setVolume(0.7f);

        hurtSound2 = soundService.create(
                "assets/audio/sound effects/Hurt 2.wav",
                false
        );
        hurtSound1.setVolume(0.8f);
    }

    public void update() {
        checkCollisionContinuous();
        camera.setPosition(move());
        camera.setRotation(rotate());
        castFireBolt();
    }

    private void castFireBolt() {
        if (inputService.getLeftMouse() == null) return;
        if (canCast && !isJumping && inputService.getLeftMouse().isPrimaryButtonDown()) {
            synchronized (collisionService.getHitBoxes()) {
                new FireBolt(position.subtract(new Vector3(0, 1, 0)), camera.forward());
                fireBoltSound.play();
                showHandAnimation();
            }

            canCast = false;
            startFireBoltTimer();
        }

        if (inputService.getRightMouse() == null) return;
        if (holdingMuffin && inputService.getLeftMouse().isPrimaryButtonDown()) {
            setHealth(MAX_HEALTH);
            handState = HandState.IDLE;
            eatSound.play();
            holdingMuffin = false;
        }
    }

    private void checkCollisionContinuous() {
        if (!invincible && !isJumping) {
            HitInfo<HitBox> hit = collider.getCollision();
            if (hit != null) {
                if (hit.getCollision().containsCollisionLayer(CollisionLayer.MUFFIN)) {
                    holdingMuffin = true;
                    handState = HandState.MUFFIN_IDLE;
                }
            }
        }
    }

    private void takeHit() {
        health -= rand.nextFloat() + 0.5f;

        if (rand.nextInt(2) == 0)
            hurtSound1.play();
        else hurtSound2.play();

        if (health <= 0) {
            if (Map.getSirHoly() != null && Map.getSirHoly().getHealth() <= 0) {
                sceneService.changeScene(SceneType.FIREBALL_CUTSCENE);
                return;
            }

            sceneService.changeScene(SceneType.GAME_OVER);
            return;
        }

        startInvincibilityTimer();
    }

    private Vector3 move() {
        Vector3 moveAmount = Vector3.zero();
        Vector3 scaledForward = new Vector3(camera.forward().scale(SPEED * configService.fixedDeltaTime));

        if (!isJumping) {
            if (inputService.getKeyDown().contains(KeyCode.W)) {
                moveAmount = moveAmount.add(scaledForward);
            } else if (inputService.getKeyDown().contains(KeyCode.S)) {
                moveAmount = moveAmount.subtract(scaledForward);
            }

            if (inputService.getKeyDown().contains(KeyCode.A)) {
                moveAmount.x += scaledForward.z;
                moveAmount.z -= scaledForward.x;
            } else if (inputService.getKeyDown().contains(KeyCode.D)) {
                moveAmount.x -= scaledForward.z;
                moveAmount.z += scaledForward.x;
            }

            if (inputService.getKeyPressed() == ' ') {
                if (moveAmount.squareMagnitude() == 0)
                    velocity = camera.forward();
                else velocity = moveAmount.normalize();

                velocity.y = JUMP_HEIGHT;
                isJumping = true;
            }
        } else {
            moveAmount = velocity.scale(JUMP_SPEED * configService.fixedDeltaTime);
        }

        HitInfo<BoxCollider> wall = checkForWallCollision(null);
        if (wall != null && moveAmount.dot(moveAmount) != 0) {
            Vector3 moveDirection = moveAmount;
            Vector3 wallRight = new Vector3(wall.getNormal().z, wall.getNormal().y, -wall.getNormal().x);

            if (moveDirection.dot(wall.getNormal()) < 0) {
                moveAmount = wallRight.scale(moveAmount.dot(wallRight));

                // This checks if the player is against an inverted corner
                HitInfo<BoxCollider> wall2 = checkForWallCollision(wall.getCollision());
                if (wall2 != null && wall2.getCollision() != wall.getCollision()) {
                    wallRight = new Vector3(wall2.getNormal().z, wall2.getNormal().y, -wall2.getNormal().x);
                    moveAmount = wallRight.scale(moveAmount.dot(wallRight));
                }

                velocity = new Vector3(0, velocity.y, 0);
            }
        }

        if (moveAmount.magnitude() != 0) {
            position = position.add(moveAmount);
            collider.setPosition(position.subtract(new Vector3(0, 0.5f, 0)));
        }

        if (isJumping) {
            float acceleration = FALL_SPEED * configService.fixedDeltaTime;
            velocity.y -= acceleration;

            if (position.y <= 0 && velocity.y <= 0) {
                velocity = Vector3.zero();
                position.y = 0;
                isJumping = false;
            }
        }

        return new Vector3(position);
    }

    private float rotate() {
        rotation += MOUSE_SENSITIVITY * inputService.getMouseMovement().x;
        return rotation;
    }

    private void startFireBoltTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                canCast = true;
            }
        }, holdingMuffin ? 1500 : 750);
    }

    private void showHandAnimation() {
        if (holdingMuffin) {
            handState = HandState.MUFFIN_FIRE;

            if (handThread != null) handThread.cancel();

            handThread = new Timer();
            handThread.schedule(new TimerTask() {
                @Override
                public void run() {
                    handState = HandState.MUFFIN_IDLE;
                }
            }, 250);
        } else {
            if (lastHandFireState == HandState.RIGHT_FIRE) {
                handState = HandState.RIGHT_FIRE;
                lastHandFireState = HandState.LEFT_FIRE;
            } else {
                handState = HandState.LEFT_FIRE;
                lastHandFireState = HandState.RIGHT_FIRE;
            }

            if (handThread != null) handThread.cancel();

            handThread = new Timer();
            handThread.schedule(new TimerTask() {
                @Override
                public void run() {
                    handState = HandState.IDLE;
                }
            }, 250);
        }

    }

    private void startInvincibilityTimer() {
        Thread invincibleTimer = new Thread(() -> {
            synchronized (this) {
                invincible = true;

                try {
                    this.wait(2000);
                } catch (InterruptedException _) {
                }

                invincible = false;
            }
        });

        invincibleTimer.start();
    }

    private HitInfo<BoxCollider> checkForWallCollision(BoxCollider ignoreWall) {
        for (HitBox wall : Map.getWalls(collider)) {
            if (ignoreWall != null && ignoreWall == wall)
                continue;

            HitInfo<BoxCollider> hit = null;
            if (wall instanceof BoxCollider boxCollider)
                hit = collider.getCollision(boxCollider);

            if (hit != null) return hit;
        }

        return null;
    }

    @Override
    public void collisionCallback(HitBox hitBox) {
        if (!invincible && !isJumping) {
            if (hitBox != null && hitBox.containsCollisionLayer(CollisionLayer.PROJECTILE_ENEMY))
                takeHit();
        }

        if (hitBox != null && hitBox.containsCollisionLayer(CollisionLayer.TRIGGER)) {
            if (inputService.getKeyDown().contains(KeyCode.E))
                sceneService.changeScene(SceneType.INTERMISSION);
        }
    }

    public Vector3 getPosition() {
        return new Vector3(position);
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public String getHandState() {
        return switch (handState) {
            case IDLE -> "file:assets/images/hands/Hands Empty.png";
            case LEFT_FIRE -> "file:assets/images/hands/Hands Left Fire.png";
            case RIGHT_FIRE -> "file:assets/images/hands/Hands Right Fire.png";
            case MUFFIN_IDLE -> "file:assets/images/hands/Hands Hold Muffin.png";
            case MUFFIN_FIRE -> "file:assets/images/hands/Hands Fire Muffin.png";
        };
    }

    private void setHealth(float value) {
        health = Math.min(Math.max(value, 0), MAX_HEALTH);
    }

    private float getHealth() {
        return health;
    }

    public SphereCollider getCollider() {
        return collider;
    }

    public float remainingHealth() {
        return health / MAX_HEALTH;
    }
}
