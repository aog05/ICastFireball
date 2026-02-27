package edu.neumont.csc150.model.projectiles;

import edu.neumont.csc150.model.colliders.CollisionEvent;
import edu.neumont.csc150.model.colliders.SphereCollider;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.view.Console;

public non-sealed class FireBolt extends Projectile implements CollisionEvent {
    public FireBolt(Vector3 position, Vector3 forward) {
        super(forward);
        collider = new SphereCollider(
                this,
                ProjectileManager.collisionService,
                null,
                new CollisionLayer[]{
                        CollisionLayer.ENEMIES,
                        CollisionLayer.ENVIRONMENT
                },
                new CollisionLayer[]{CollisionLayer.PROJECTILE_PLAYER}
        );
        collider.setPosition(new Vector3(position));
        collider.setRadius(0.25f);
        collider.setColor(Console.TextColor.YELLOW);
    }
}
