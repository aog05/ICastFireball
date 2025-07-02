package edu.neumont.csc150.model.colliders;

import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.enums.RenderLayer;
import edu.neumont.csc150.service.Injectable;

public non-sealed class SphereCollider extends HitBox {
    private float radius = 1;

    public SphereCollider(CollisionEvent event, Injectable collision) {
        super(event, collision);
    }

    public SphereCollider(
            CollisionEvent event,
            Injectable collision,
            RenderLayer[] renderLayers,
            CollisionLayer[] canCollideWith,
            CollisionLayer[] collisionLayers
    ) {
        super(event, collision, renderLayers, canCollideWith, collisionLayers);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
