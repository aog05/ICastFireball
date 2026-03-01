package edu.neumont.csc150.model.colliders;

import edu.neumont.csc150.model.enums.*;
import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.service.ColorService;
import edu.neumont.csc150.service.Injectable;
import edu.neumont.csc150.view.Console;
import javafx.scene.image.Image;

public non-sealed class SpriteCollider extends HitBox {
    private final ColorService colorService;
    private final Vector3[] verts = new Vector3[4];
    private final Vector3[] originalVerts = new Vector3[4];
    private Image image;
    private Vector3 scale = new Vector3(1, 1, 1);
    public boolean showImage = true;
    /** In radians*/
    private float rotation;

    public SpriteCollider(Injectable collision, Injectable color, String imageURL) {
        super(null, collision);
        collisionLayers.add(CollisionLayer.NONE);
        canCollideWithLayers.add(CollisionLayer.NONE);
        this.colorService = (ColorService) color;
        image = colorService.loadImage(imageURL);
        initializeVertices();
    }

    public SpriteCollider(
            Injectable collision,
            Injectable color,
            Vector3 position,
            float rotation,
            Vector3 scale,
            String imageURL
    ) {
        this(collision, color, imageURL);
        setPosition(position);
        setScale(scale);
        setRotation(rotation);
    }

    public SpriteCollider(
            Injectable collision,
            Injectable color,
            String imageURL,
            RenderLayer[] renderLayers
    ) {
        super(
                null,
                collision,
                renderLayers,
                new CollisionLayer[]{ CollisionLayer.NONE },
                new CollisionLayer[]{ CollisionLayer.NONE }
        );
        this.colorService = (ColorService) color;
        image = colorService.loadImage(imageURL);
        initializeVertices();
    }

    private void initializeVertices() {
        verts[0] = new Vector3(position.x - scale.x / 2, position.y - scale.y / 2, position.z);
        verts[1] = new Vector3(position.x + scale.x / 2, position.y - scale.y / 2, position.z);
        verts[2] = new Vector3(position.x + scale.x / 2, position.y + scale.y / 2, position.z);
        verts[3] = new Vector3(position.x - scale.x / 2, position.y + scale.y / 2, position.z);

        originalVerts[0] = new Vector3(-scale.x / 2, -scale.y / 2, 0);
        originalVerts[1] = new Vector3(+scale.x / 2, -scale.y / 2, 0);
        originalVerts[2] = new Vector3(+scale.x / 2, +scale.y / 2, 0);
        originalVerts[3] = new Vector3(-scale.x / 2, +scale.y / 2, 0);
    }

    private void updateVerts() {
        verts[0].x = position.x - scale.x / 2; verts[0].y = position.y - scale.y / 2; verts[0].z = position.z;
        verts[1].x = position.x + scale.x / 2; verts[1].y = position.y - scale.y / 2; verts[1].z = position.z;
        verts[2].x = position.x + scale.x / 2; verts[2].y = position.y + scale.y / 2; verts[2].z = position.z;
        verts[3].x = position.x - scale.x / 2; verts[3].y = position.y + scale.y / 2; verts[3].z = position.z;

        originalVerts[0].x = -scale.x / 2; originalVerts[0].y = -scale.y / 2; originalVerts[0].z = 0;
        originalVerts[1].x = +scale.x / 2; originalVerts[1].y = -scale.y / 2; originalVerts[1].z = 0;
        originalVerts[2].x = +scale.x / 2; originalVerts[2].y = +scale.y / 2; originalVerts[2].z = 0;
        originalVerts[3].x = -scale.x / 2; originalVerts[3].y = +scale.y / 2; originalVerts[3].z = 0;
    }

    public Vector3 getNormal() {
        return verts[2].subtract(verts[0]).cross(verts[1].subtract(verts[0])).normalize();
    }

    public Vector3 getRight() {
        Vector3 normal = getNormal();
        return new Vector3(normal.z, normal.y, -normal.x);
    }

    public void setImage(String imageURL) {
        image = colorService.loadImage(imageURL);
    }

    @Override
    public void setPosition(Vector3 position) {
        this.position = position;
        updateVerts();
    }

    public Color getColor(float x, float y) {
        if (image == null || !showImage) return null;

        int xCoordinate = (int) ((x / 2 + 0.5f) * image.getWidth());
        int yCoordinate = (int) ((y / 2 + 0.5f) * image.getHeight());
        return colorService.getColorAtPoint(image, xCoordinate, yCoordinate);
    }

    @Override
    public Color getColor() {
        throw new IllegalStateException("Must provide x and y coordinates");
    }

    @Override
    public void setColor(Console.TextColor color) {
        throw new IllegalStateException("Cannot set color of the sprite");
    }

    public Vector3 getScale() {
        return new Vector3(scale);
    }

    public void setScale(Vector3 scale) {
        this.scale = scale;
        updateVerts();
    }

    public Vector3[] getVerts() {
        if (verts == null) return null;

        return verts.clone();
    }

    public float getRotation() {
        return (float) Math.toDegrees(rotation);
    }

    /** Supply rotation in degrees */
    public void setRotation(float rotation) {
        this.rotation = (float) Math.toRadians(rotation);

        for (int i = 0; i < verts.length; i++) {
            Vector3 vert = verts[i];
            float oX = originalVerts[i].x;
            float oZ = originalVerts[i].z;
            vert.x = (float) (oX * Math.cos(this.rotation) - oZ * Math.sin(this.rotation) + position.x);
            vert.z = (float) (oX * Math.sin(this.rotation) + oZ * Math.cos(this.rotation) + position.z);
        }
    }
}
