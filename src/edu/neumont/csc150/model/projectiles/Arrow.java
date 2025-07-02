package edu.neumont.csc150.model.projectiles;

import edu.neumont.csc150.model.colliders.CollisionEvent;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.colliders.SphereCollider;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.model.misc.Vector3;

public non-sealed class Arrow extends Projectile implements CollisionEvent {
    public Arrow(Vector3 position, Vector3 forward) {
        super(forward);
        collider = new SphereCollider(
                this,
                ProjectileManager.collisionService,
                null,
                new CollisionLayer[]{
                        CollisionLayer.PLAYER,
                        CollisionLayer.ENVIRONMENT
                },
                new CollisionLayer[]{CollisionLayer.PROJECTILE_ENEMY}
        );
        collider.setPosition(new Vector3(position));
        collider.setRadius(0.25f);
        collider.setColor(new Color(0x79444a));
    }

    @Override
    public void collisionCallback(HitBox hitBox) {}
}
