package edu.neumont.csc150.model.colliders;

import edu.neumont.csc150.model.enums.*;
import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.model.misc.raycast.HitInfo;
import edu.neumont.csc150.service.CollisionService;
import edu.neumont.csc150.service.Injectable;
import edu.neumont.csc150.view.Console;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public sealed abstract class HitBox permits BoxCollider, SphereCollider, SpriteCollider {
    protected CollisionService collisionService;
    private CollisionEvent event;
    protected Vector3 position = Vector3.zero();
    protected Color color = new Color(Console.TextColor.WHITE);
    protected final Set<RenderLayer> renderLayers = new HashSet<>();
    /**
     * Layers that this hitbox can detect and collide with
     */
    protected final Set<CollisionLayer> canCollideWithLayers = new HashSet<>();
    /**
     * Layers that this hitbox can be hit on
     */
    protected final Set<CollisionLayer> collisionLayers = new HashSet<>();

    protected HitBox(CollisionEvent event, Injectable collision) {
        collisionService = (CollisionService) collision;
        collisionService.addHitBox(this);
        this.event = event;
    }

    protected HitBox(
            CollisionEvent event,
            Injectable collision,
            RenderLayer[] renderLayers,
            CollisionLayer[] canCollideWith,
            CollisionLayer[] collisionLayers
    ) {
        this(event, collision);

        if (renderLayers != null)
            this.renderLayers.addAll(Arrays.asList(renderLayers));

        if (canCollideWith != null)
            this.canCollideWithLayers.addAll(Arrays.asList(canCollideWith));

        if (collisionLayers != null)
            this.collisionLayers.addAll(Arrays.asList(collisionLayers));
    }

    public HitInfo<HitBox> getCollision() {
        for (HitBox compareTo : collisionService.getHitBoxes(this)) {
            HitInfo<HitBox> hit = collisionService.isColliding(this, compareTo);
            if (hit == null) continue;

            return new HitInfo<>(
                    hit.getHitPosition(),
                    hit.getColor(),
                    hit.getDistance(),
                    hit.getCollision()
            );
        }

        return null;
    }

    public HitInfo<HitBox> getCollision(HitBox compareTo) {
        if (compareTo == null) return null;
        if (compareTo.getCollisionLayers()[0] == CollisionLayer.NONE) return null;

        if (compareTo instanceof BoxCollider boxCollider) {
            HitInfo<BoxCollider> boxHit = getCollision(boxCollider);

            if (boxHit == null) return null;

            return new HitInfo<>(
                    boxHit.getHitPosition(),
                    boxHit.getColor(),
                    boxHit.getDistance(),
                    boxHit.getCollision()
            );
        } else if (compareTo instanceof SphereCollider sphereCollider) {
            HitInfo<SphereCollider> sphereHit = getCollision(sphereCollider);

            if (sphereHit == null) return null;

            return new HitInfo<>(
                    sphereHit.getHitPosition(),
                    sphereHit.getColor(),
                    sphereHit.getDistance(),
                    sphereHit.getCollision()
            );
        }

        return null;
    }

    public HitInfo<BoxCollider> getCollision(BoxCollider compareTo) {
        HitInfo<HitBox> hit = collisionService.isColliding(this, compareTo);
        if (hit == null) return null;

        return new HitInfo<>(
                hit.getHitPosition(),
                hit.getColor(),
                hit.getDistance(),
                (BoxCollider) hit.getCollision(),
                hit.getNormal()
        );
    }

    public HitInfo<SphereCollider> getCollision(SphereCollider compareTo) {
        HitInfo<HitBox> hit = collisionService.isColliding(this, compareTo);
        if (hit == null) return null;

        return new HitInfo<>(
                hit.getHitPosition(),
                hit.getColor(),
                hit.getDistance(),
                (SphereCollider) hit.getCollision()
        );
    }

    public boolean containsRenderLayer(RenderLayer renderLayer) {
        return renderLayers.contains(renderLayer);
    }

    public boolean containsRenderLayer(RenderLayer[] renderLayers) {
        for (RenderLayer renderLayer : renderLayers) {
            if (containsRenderLayer(renderLayer)) return true;
        }

        return false;
    }

    public boolean containsCollisionLayer(CollisionLayer collisionLayer) {
        return collisionLayers.contains(collisionLayer);
    }

    public boolean containsCollisionLayer(CollisionLayer[] collisionLayers) {
        for (CollisionLayer collisionLayer : collisionLayers) {
            if (containsCollisionLayer(collisionLayer)) return true;
        }

        return false;
    }

    public boolean canCollideWithLayer(CollisionLayer collisionLayer) {
        return canCollideWithLayers.contains(collisionLayer);
    }

    public boolean canCollideWithLayer(CollisionLayer[] collisionLayers) {
        for (CollisionLayer collisionLayer : collisionLayers) {
            if (canCollideWithLayer(collisionLayer)) return true;
        }

        return false;
    }

    public void callback(HitBox hitBox) {
        if (event == null) return;

        event.collisionCallback(hitBox);
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public void setColor(Console.TextColor color) {
        this.color.setConsoleColor(color);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return new Color(color);
    }

    public RenderLayer[] getRenderLayers() {
        return renderLayers.toArray(new RenderLayer[0]);
    }

    public CollisionLayer[] getLayersCanCollideWith() {
        return canCollideWithLayers.toArray(new CollisionLayer[0]);
    }

    public CollisionLayer[] getCollisionLayers() {
        return collisionLayers.toArray(new CollisionLayer[0]);
    }
}
