package edu.neumont.csc150.model.goblins;

import edu.neumont.csc150.controller.Map;
import edu.neumont.csc150.model.Muffin;
import edu.neumont.csc150.model.colliders.*;
import edu.neumont.csc150.model.enums.*;
import edu.neumont.csc150.model.misc.AudioPlayer;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.model.misc.raycast.HitInfo;
import edu.neumont.csc150.model.misc.raycast.Raycast;
import edu.neumont.csc150.service.*;

import java.util.Timer;
import java.util.TimerTask;

public sealed abstract class Goblin implements CollisionEvent permits GoblinPaladin, GoblinRanger {
    protected final CollisionService collisionService;
    protected final ColorService colorService;
    protected final ConfigService configService;
    protected final SoundService soundService;
    private int health;
    protected Vector3 position = Vector3.zero();
    private final SphereCollider collider;
    private final SpriteCollider sprite;
    public boolean hasMuffin = false;
    private boolean isInvincible = false;
    private final Timer invincibleTimer = new Timer();
    private final AudioPlayer deathSound;

    protected Goblin(
            Injectable collision,
            Injectable color,
            Injectable config,
            Injectable sound,
            String spriteURL,
            int health,
            Vector3 position
    ) {
        collisionService = (CollisionService) collision;
        colorService = (ColorService) color;
        configService = (ConfigService) config;
        soundService = (SoundService) sound;
        collider = new SphereCollider(
                this,
                collision,
                new RenderLayer[]{RenderLayer.COLLISION_ONLY},
                new CollisionLayer[]{CollisionLayer.PROJECTILE_PLAYER},
                new CollisionLayer[]{CollisionLayer.ENEMIES}
        );
        collider.setRadius(0.75f);
        sprite = new SpriteCollider(collisionService, colorService, spriteURL);
        sprite.setScale(new Vector3(3, 4, 0));

        this.health = health;
        setPosition(position);
        deathSound = soundService.create(
                "assets/audio/sound effects/Goblin Death.wav",
                false
        );
        deathSound.setVolume(0.75f);
    }

    public void update(Vector3 playerPosition) {
        faceTowardPlayer(playerPosition);
    }

    private void faceTowardPlayer(Vector3 playerPosition) {
        Vector3 offset = playerPosition.subtract(getPosition());
        float angle = (float) Math.toDegrees(Math.atan2(offset.z, offset.x));
        sprite.setRotation(angle + 90);
    }

    protected abstract void attack(Vector3 playerPosition);

    protected void damage() {
        if (isInvincible) return;

        health--;
        isInvincible = true;

        if (health <= 0) {
            kill();
            return;
        }

        invincibleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isInvincible = false;
            }
        }, 750);
    }

    public void kill() {
        deathSound.play();
        collisionService.removeHitBox(collider);
        collisionService.removeHitBox(sprite);
        Map.removeGoblin(this);

        if (hasMuffin)
            new Muffin(getPosition().subtract(new Vector3(0, 1, 0)), collisionService, colorService);
    }

    protected boolean canSeePlayer(Vector3 playerPosition) {
        Raycast raycast = new Raycast(getPosition(), playerPosition.subtract(getPosition()));
        HitInfo<HitBox> hit = collisionService.raycastHit(raycast,
                new CollisionLayer[]{ CollisionLayer.ENVIRONMENT, CollisionLayer.PLAYER }
        );
        return hit != null && hit.getCollision().containsCollisionLayer(CollisionLayer.PLAYER);
    }

    protected void setImage(String imageURL) {
        sprite.setImage(imageURL);
    }

    @Override
    public void collisionCallback(HitBox hitBox) {
        if (hitBox.containsCollisionLayer(CollisionLayer.PROJECTILE_PLAYER))
            damage();
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        collider.setPosition(position.add(new Vector3(0, -1, 0)));
        sprite.setPosition(position.add(new Vector3(0, -1, 0)));
    }

    public void setScale(Vector3 scale) {
        sprite.setScale(scale);
        collider.setRadius(scale.magnitude() / 2);
    }

    protected boolean isInvincible() {
        return isInvincible;
    }
}
