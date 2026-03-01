package edu.neumont.csc150.model.colliders;

import edu.neumont.csc150.model.enums.*;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.service.Injectable;

public non-sealed class BoxCollider extends HitBox {
    private final Vector3[] verts = new Vector3[8];
    private final Vector3[] originalVerts = new Vector3[8];
    private final Vector3[][] quads = new Vector3[6][4];
    private Vector3 scale = new Vector3(1, 1, 1);
    /** In radians*/
    private float rotation;

    public BoxCollider(CollisionEvent event, Injectable collision) {
        super(event, collision);
        initializeVertices();
    }

    public BoxCollider(
            CollisionEvent event,
            Injectable collision,
            RenderLayer[] renderLayers,
            CollisionLayer[] canCollideWith,
            CollisionLayer[] collisionLayers
    ) {
        super(event, collision, renderLayers, canCollideWith, collisionLayers);
        initializeVertices();
    }

    private void initializeVertices() {
        verts[0] = new Vector3(position.x - scale.x / 2, position.y - scale.y / 2, position.z - scale.z / 2);
        verts[1] = new Vector3(position.x + scale.x / 2, position.y - scale.y / 2, position.z - scale.z / 2);
        verts[2] = new Vector3(position.x + scale.x / 2, position.y + scale.y / 2, position.z - scale.z / 2);
        verts[3] = new Vector3(position.x - scale.x / 2, position.y + scale.y / 2, position.z - scale.z / 2);
        verts[4] = new Vector3(position.x - scale.x / 2, position.y - scale.y / 2, position.z + scale.z / 2);
        verts[5] = new Vector3(position.x + scale.x / 2, position.y - scale.y / 2, position.z + scale.z / 2);
        verts[6] = new Vector3(position.x + scale.x / 2, position.y + scale.y / 2, position.z + scale.z / 2);
        verts[7] = new Vector3(position.x - scale.x / 2, position.y + scale.y / 2, position.z + scale.z / 2);

        originalVerts[0] = new Vector3(-scale.x / 2, -scale.y / 2, -scale.z / 2);
        originalVerts[1] = new Vector3(+scale.x / 2, -scale.y / 2, -scale.z / 2);
        originalVerts[2] = new Vector3(+scale.x / 2, +scale.y / 2, -scale.z / 2);
        originalVerts[3] = new Vector3(-scale.x / 2, +scale.y / 2, -scale.z / 2);
        originalVerts[4] = new Vector3(-scale.x / 2, -scale.y / 2, +scale.z / 2);
        originalVerts[5] = new Vector3(+scale.x / 2, -scale.y / 2, +scale.z / 2);
        originalVerts[6] = new Vector3(+scale.x / 2, +scale.y / 2, +scale.z / 2);
        originalVerts[7] = new Vector3(-scale.x / 2, +scale.y / 2, +scale.z / 2);

        quads[0] = new Vector3[]{ verts[0], verts[3], verts[2], verts[1] };
        quads[1] = new Vector3[]{ verts[4], verts[5], verts[6], verts[7] };
        quads[2] = new Vector3[]{ verts[1], verts[5], verts[6], verts[2] };
        quads[3] = new Vector3[]{ verts[0], verts[3], verts[7], verts[4] };
        quads[4] = new Vector3[]{ verts[0], verts[1], verts[5], verts[4] };
        quads[5] = new Vector3[]{ verts[3], verts[2], verts[6], verts[7] };
    }

    public Vector3[] getVerts() {
        return verts.clone();
    }

    public Vector3[][] getQuads() {
        if (quads == null) return null;

        return quads.clone();
    }

    private void updateVerts() {
        verts[0].x = position.x - scale.x / 2; verts[0].y = position.y - scale.y / 2; verts[0].z = position.z - scale.z / 2;
        verts[1].x = position.x + scale.x / 2; verts[1].y = position.y - scale.y / 2; verts[1].z = position.z - scale.z / 2;
        verts[2].x = position.x + scale.x / 2; verts[2].y = position.y + scale.y / 2; verts[2].z = position.z - scale.z / 2;
        verts[3].x = position.x - scale.x / 2; verts[3].y = position.y + scale.y / 2; verts[3].z = position.z - scale.z / 2;
        verts[4].x = position.x - scale.x / 2; verts[4].y = position.y - scale.y / 2; verts[4].z = position.z + scale.z / 2;
        verts[5].x = position.x + scale.x / 2; verts[5].y = position.y - scale.y / 2; verts[5].z = position.z + scale.z / 2;
        verts[6].x = position.x + scale.x / 2; verts[6].y = position.y + scale.y / 2; verts[6].z = position.z + scale.z / 2;
        verts[7].x = position.x - scale.x / 2; verts[7].y = position.y + scale.y / 2; verts[7].z = position.z + scale.z / 2;

        originalVerts[0].x = -scale.x / 2; originalVerts[0].y = -scale.y / 2; originalVerts[0].z = -scale.z / 2;
        originalVerts[1].x = +scale.x / 2; originalVerts[1].y = -scale.y / 2; originalVerts[1].z = -scale.z / 2;
        originalVerts[2].x = +scale.x / 2; originalVerts[2].y = +scale.y / 2; originalVerts[2].z = -scale.z / 2;
        originalVerts[3].x = -scale.x / 2; originalVerts[3].y = +scale.y / 2; originalVerts[3].z = -scale.z / 2;
        originalVerts[4].x = -scale.x / 2; originalVerts[4].y = -scale.y / 2; originalVerts[4].z = +scale.z / 2;
        originalVerts[5].x = +scale.x / 2; originalVerts[5].y = -scale.y / 2; originalVerts[5].z = +scale.z / 2;
        originalVerts[6].x = +scale.x / 2; originalVerts[6].y = +scale.y / 2; originalVerts[6].z = +scale.z / 2;
        originalVerts[7].x = -scale.x / 2; originalVerts[7].y = +scale.y / 2; originalVerts[7].z = +scale.z / 2;
    }

    @Override
    public void setPosition(Vector3 position) {
        this.position = position;
        updateVerts();
    }

    public Vector3 getScale() {
        return new Vector3(scale);
    }

    public void setScale(Vector3 scale) {
        this.scale = scale;
        updateVerts();
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

    /** Returns all positive normals to the surface of the rectangle */
    public Vector3[] getNormals() {
        Vector3 localX = quads[2][2].subtract(quads[2][0]).cross(quads[2][1].subtract(quads[2][0]));
        Vector3 localY = quads[5][2].subtract(quads[5][0]).cross(quads[5][1].subtract(quads[5][0]));
        Vector3 localZ = localX.cross(localY);

        localX = localX.normalize();
        localY = localY.normalize();
        localZ = localZ.normalize();

        return new Vector3[]{ localX, localY, localZ };
    }

    /** Returns all surface normals of the rectangle, even the negative ones */
    public Vector3[] getAllNormals() {
        Vector3 localX = quads[2][2].subtract(quads[2][0]).cross(quads[2][1].subtract(quads[2][0]));
        Vector3 localY = quads[5][2].subtract(quads[5][0]).cross(quads[5][1].subtract(quads[5][0]));
        Vector3 localZ = localX.cross(localY);

        localX = localX.normalize();
        localY = localY.normalize();
        localZ = localZ.normalize();

        return new Vector3[]{
                localX,
                localY,
                localZ,
                localX.scale(-1),
                localY.scale(-1),
                localZ.scale(-1)
        };
    }
}
