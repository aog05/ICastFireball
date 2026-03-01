package edu.neumont.csc150.model;

import edu.neumont.csc150.controller.Map;
import edu.neumont.csc150.model.colliders.CollisionEvent;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.colliders.SphereCollider;
import edu.neumont.csc150.model.colliders.SpriteCollider;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.enums.RenderLayer;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.service.*;

public class Muffin implements CollisionEvent {
    private Vector3 position;
    private final SpriteCollider sprite;
    private final SphereCollider collider;
    private final CollisionService collisionService;
    private final ColorService colorService;

    public Muffin(Vector3 position, Injectable collision, Injectable color) {
        collisionService = (CollisionService) collision;
        colorService = (ColorService) color;
        sprite = new SpriteCollider(
                collisionService,
                colorService,
                "file:assets/images/Muffin.png"
        );
        collider = new SphereCollider(
                this,
                collisionService,
                new RenderLayer[]{RenderLayer.COLLISION_ONLY},
                new CollisionLayer[]{CollisionLayer.PLAYER},
                new CollisionLayer[]{CollisionLayer.MUFFIN}
        );
        sprite.setScale(new Vector3(1, 2, 0));
        collider.setRadius(2);
        setPosition(position);
        Map.addMuffin(this);
        despawnTimer();
    }

    public void update(Vector3 playerPosition) {
        faceTowardPlayer(playerPosition);
    }

    private void faceTowardPlayer(Vector3 playerPosition) {
        Vector3 offset = playerPosition.subtract(getPosition());
        float angle = (float) Math.toDegrees(Math.atan2(offset.z, offset.x));
        sprite.setRotation(angle + 90);
    }

    private void despawnTimer() {
        new Thread(() -> {
            synchronized (this) {
                try {
                    this.wait(5000);
                    sprite.showImage = false;
                    this.wait(750);
                    sprite.showImage = true;

                    this.wait(2450);
                    sprite.showImage = false;
                    this.wait(750);

                    for (int i = 0; i < 8; i++) {
                        sprite.showImage = true;
                        this.wait(250);
                        sprite.showImage = false;
                        this.wait(250);
                    }

                    removeMuffin();
                } catch (InterruptedException _) {}
            }
        }).start();
    }

    public void removeMuffin() {
        collisionService.removeHitBox(sprite);
        collisionService.removeHitBox(collider);
        Map.removeMuffin(this);
    }

    public Vector3 getPosition() {
        return new Vector3(position);
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        sprite.setPosition(position);
        collider.setPosition(position);
    }

    @Override
    public void collisionCallback(HitBox hitBox) {
        removeMuffin();
    }
}
