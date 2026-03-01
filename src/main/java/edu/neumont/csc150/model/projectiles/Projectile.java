package edu.neumont.csc150.model.projectiles;

import edu.neumont.csc150.model.colliders.CollisionEvent;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.colliders.SphereCollider;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.model.misc.raycast.HitInfo;
import edu.neumont.csc150.service.ConfigService;
import edu.neumont.csc150.view.Console;

import java.util.Timer;
import java.util.TimerTask;

public sealed abstract class Projectile implements CollisionEvent permits FireBolt, Arrow {
    protected final ConfigService configService;
    protected final Timer timer = new Timer();
    protected SphereCollider collider;
    protected final Vector3 forward;
    protected final float SPEED = 15;

    protected Projectile(Vector3 forward) {
        configService = ProjectileManager.configService;

        this.forward = new Vector3(forward);
        ProjectileManager.add(this);

        Projectile instance = this;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ProjectileManager.remove(instance);
            }
        }, 5000);
    }

    void action() {
        if (collider == null) return;

        Vector3 moveBy = forward.scale(SPEED * configService.fixedDeltaTime);
        collider.setPosition(collider.getPosition().add(moveBy));

        HitInfo<HitBox> collision = collider.getCollision();
        if (collision != null) {
            timer.cancel();
            collision.getCollision().callback(collider);
            ProjectileManager.remove(this);
        }
    }

    @Override
    public void collisionCallback(HitBox hitBox) {}

    SphereCollider getCollider() {
        return collider;
    }
}
