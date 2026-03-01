package edu.neumont.csc150.model.misc.raycast;

import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.model.misc.Vector3;

public class HitInfo<T extends HitBox> {
    private final Vector3 hitPosition;
    private final Color color;
    private final float distance;
    private final T collision;
    private final Vector3 normal;

    public HitInfo(Vector3 position, Color color, float distance, T collision) {
        this.hitPosition = position;
        this.color = color;
        this.distance = distance;
        this.collision = collision;
        this.normal = Vector3.zero();
    }

    public HitInfo(Vector3 position, Color color, float distance, T collision, Vector3 normal) {
        this.hitPosition = position;
        this.color = color;
        this.distance = distance;
        this.collision = collision;
        this.normal = normal;
    }

    public Vector3 getHitPosition() {
        return hitPosition;
    }

    public Color getColor() {
        return new Color(color);
    }

    public float getDistance() {
        return distance;
    }

    public T getCollision() {
        return collision;
    }

    public Vector3 getNormal() {
        return normal;
    }
}
