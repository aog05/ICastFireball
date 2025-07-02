package edu.neumont.csc150.model;

import edu.neumont.csc150.model.colliders.BoxCollider;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.service.Injectable;
import edu.neumont.csc150.view.Console;

public class Wall {
    private final BoxCollider collider;

    public Wall(Injectable collision) {
        collider = new BoxCollider(
                null,
                collision,
                null,
                null,
                new CollisionLayer[]{CollisionLayer.ENVIRONMENT}
        );
        setColor(Console.TextColor.WHITE);
    }

    public Wall(Injectable collision, Vector3 pos, float rotation, Vector3 scale) {
        this(collision);
        setPosition(pos);
        setScale(scale);
        setRotation(rotation);
    }

    public Wall(Injectable collision, Vector3 pos, float rotation, Vector3 scale, Console.TextColor color) {
        this(collision, pos, rotation, scale);
        setColor(color);
    }

    public void setColor(Console.TextColor color) {
        collider.setColor(color);
    }

    public Color getColor() {
        return collider.getColor();
    }

    /** Set rotation in degrees */
    public void setRotation(float rotation) {
        collider.setRotation(rotation);
    }

    public float getRotation() {
        return collider.getRotation();
    }

    public void setPosition(Vector3 v) {
        collider.setPosition(v);
    }

    public Vector3 getPosition() {
        return collider.getPosition();
    }

    public void setScale(Vector3 scale) {
        collider.setScale(scale);
    }

    public Vector3 getScale() {
        return collider.getScale();
    }

    public BoxCollider getBoxCollider() {
        return collider;
    }
}
