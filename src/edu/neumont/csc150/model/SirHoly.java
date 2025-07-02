package edu.neumont.csc150.model;

import edu.neumont.csc150.model.colliders.CollisionEvent;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.colliders.SphereCollider;
import edu.neumont.csc150.model.colliders.SpriteCollider;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.enums.RenderLayer;
import edu.neumont.csc150.model.misc.AudioPlayer;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.model.projectiles.Arrow;
import edu.neumont.csc150.service.*;

public class SirHoly implements CollisionEvent {
    private final float SPEED = 1;
    private final Vector3 STARTING_POSITION;

    private Vector3 position;
    private final SpriteCollider sprite;
    private final SphereCollider collider;
    private final CollisionService collisionService;
    private final ColorService colorService;
    private final ConfigService configService;
    private final SoundService soundService;
    private volatile boolean isAttacking = false;
    private Thread attackThread;
    private Vector3 moveTo;
    private float jumpVelocity;
    private int health = 10;
    private final AudioPlayer arrowShotSound;
    private final AudioPlayer hitSound;
    private final AudioPlayer itsNotSerious;

    public SirHoly(
            Vector3 position,
            Injectable collision,
            Injectable color,
            Injectable config,
            Injectable sound
    ) {
        collisionService = (CollisionService) collision;
        colorService = (ColorService) color;
        configService = (ConfigService) config;
        soundService = (SoundService) sound;
        sprite = new SpriteCollider(
                collisionService,
                colorService,
                "file:assets/images/knight/Knoble Knight Idle.png"
        );
        sprite.setScale(new Vector3(6, 12, 0));

        collider = new SphereCollider(
                this,
                collisionService,
                new RenderLayer[]{RenderLayer.COLLISION_ONLY},
                new CollisionLayer[]{CollisionLayer.PROJECTILE_PLAYER},
                new CollisionLayer[]{CollisionLayer.ENEMIES}
        );
        collider.setRadius(2);
        setPosition(position);
        STARTING_POSITION = position;
        moveTo = STARTING_POSITION;

        arrowShotSound = soundService.create(
                "assets/audio/sound effects/Arrow Shot.wav",
                false
        );
        arrowShotSound.setVolume(0.9f);
        hitSound = soundService.create(
                "assets/audio/sound effects/Knight Hit.wav",
                false
        );
        hitSound.setVolume(0.9f);

        soundService.create(
                "assets/audio/knight/Why In My Castle.wav",
                false
        ).play();

        itsNotSerious = soundService.create(
                "assets/audio/knight/Bro It's Not That Serious.wav",
                false
        );
    }

    public void update(Vector3 playerPosition) {
        Vector3 groundPosition = new Vector3(getPosition().x, 0, getPosition().z);
        if (Vector3.squaredDistance(groundPosition, moveTo) > 0.5f) {
            Vector3 finalPosition = moveTo.subtract(getPosition());
            setPosition(getPosition().add(finalPosition.scale(SPEED * configService.fixedDeltaTime)));
        }

        jump();

        if (!isAttacking) attack();

        faceTowardPlayer(playerPosition);
    }

    private void jump() {
        setPosition(getPosition().add(new Vector3(0, jumpVelocity * configService.fixedDeltaTime, 0)));
        setPosition(new Vector3(
                getPosition().x,
                getPosition().y < 0 ? 0 : getPosition().y,
                getPosition().z)
        );
        jumpVelocity -= 10 * configService.fixedDeltaTime;
    }

    private void attack() {
        isAttacking = true;

        attackThread = new Thread(() -> {
            synchronized (this) {
                try {
                    moveTo = randomMovement();

                    int walkCycles = 6;
                    for (int i = 0; i < walkCycles; i++) {
                        if (i % 2 == 0)
                            sprite.setImage("file:assets/images/knight/Knoble Knight Walk 1.png");
                        else sprite.setImage("file:assets/images/knight/Knoble Knight Walk 2.png");

                        this.wait(2000 / walkCycles);
                    }

                    for (int i = 0; i < 3; i++) {
                        jumpVelocity = 8;
                        sprite.setImage("file:assets/images/knight/Knoble Knight Jump 2.png");
                        this.wait(1800);
                        shootArrows();
                        arrowShotSound.play();
                        sprite.setImage("file:assets/images/knight/Knoble Knight Jump 1.png");
                        this.wait(200);
                    }

                    isAttacking = false;
                } catch (InterruptedException _) {
                }
            }
        });

        attackThread.start();
    }

    private void shootArrows() {
        int arrowAmount = 15;
        double randomOffset = Math.random();
        for (int i = 0; i < arrowAmount; i++) {
            float angle = (float) (Math.PI * 2 + randomOffset) * i / arrowAmount;
            float XForward = (float) Math.cos(angle);
            float ZForward = (float) Math.sin(angle);
            new Arrow(getPosition().subtract(new Vector3(0, 1, 0)), new Vector3(XForward, 0, ZForward));
        }
    }

    private Vector3 randomMovement() {
        if (Math.random() > 0.5) {
            double randomAngle = Math.random() * Math.PI * 2;
            return new Vector3((float) Math.cos(randomAngle), 0, (float) Math.sin(randomAngle))
                    .scale(5)
                    .add(STARTING_POSITION);
        }

        return STARTING_POSITION;
    }

    private void faceTowardPlayer(Vector3 playerPosition) {
        Vector3 offset = playerPosition.subtract(getPosition());
        float angle = (float) Math.toDegrees(Math.atan2(offset.z, offset.x));
        sprite.setRotation(angle + 90);
    }

    public Vector3 getPosition() {
        return new Vector3(position);
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        sprite.setPosition(this.position);
        collider.setPosition(this.position);
    }

    public void unload() {
        collisionService.removeHitBox(sprite);
        collisionService.removeHitBox(collider);
        attackThread.interrupt();
    }

    @Override
    public void collisionCallback(HitBox hitBox) {
        if (hitBox.containsCollisionLayer(CollisionLayer.PROJECTILE_PLAYER)) {
            health--;

            if (health == 5) itsNotSerious.play();
            else hitSound.play();

            sprite.setImage("file:assets/images/knight/Knoble Knight Hurt.png");
        }
    }

    public int getHealth() {
        return health;
    }
}
