package edu.neumont.csc150.model.misc.raycast;

import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.enums.RenderLayer;
import edu.neumont.csc150.model.misc.Vector3;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Raycast {
    public Vector3 position;
    private Vector3 direction;
    public final float DISTANCE;
    private final Set<RenderLayer> renderLayer = new HashSet<>();
    private final Set<CollisionLayer> collisionLayer = new HashSet<>();

    public Raycast() {
        DISTANCE = Float.MAX_VALUE;
    }

    public Raycast(float distance) {
        DISTANCE = distance;
    }

    public Raycast(Vector3 position, Vector3 direction) {
        this();
        this.position = position;
        setDirection(direction);
    }

    public Raycast(Vector3 position, Vector3 direction, float distance) {
        this(distance);
        this.position = position;
        setDirection(direction);
    }

    public Raycast(
            Vector3 position,
            Vector3 direction,
            float distance,
            RenderLayer[] layers,
            CollisionLayer[] collisionLayers
    ) {
        this(position, direction, distance);
        renderLayer.addAll(Arrays.asList(layers));
        collisionLayer.addAll(Arrays.asList(collisionLayers));
    }

    public Vector3 getDirection() {
        return new Vector3(direction);
    }

    public void setDirection(Vector3 direction) {
        this.direction = direction.normalize();
    }

    public boolean containsRenderLayer(RenderLayer layer) {
        return renderLayer.contains(layer);
    }

    public boolean containsCollisionLayer(CollisionLayer layer) {
        return collisionLayer.contains(layer);
    }
}
