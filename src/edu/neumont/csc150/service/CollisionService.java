package edu.neumont.csc150.service;

import edu.neumont.csc150.model.colliders.*;
import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.enums.RenderLayer;
import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.model.misc.Quadtree;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.model.misc.raycast.HitInfo;
import edu.neumont.csc150.model.misc.raycast.Raycast;
import edu.neumont.csc150.view.Console;

import java.awt.Rectangle;
import java.util.*;

public class CollisionService implements Injectable {
    private final Set<HitBox> hitBoxes = new LinkedHashSet<>();
    private final Quadtree quadtree = new Quadtree(1, new Rectangle(-100, -100, 200, 200));

    @Override
    public void startService() {
    }

    @Override
    public void stopService() {
    }

    public HitInfo<HitBox> isColliding(HitBox hitBox1, HitBox hitBox2) {
        if (hitBox1 == hitBox2) return null;

        if (!hitBox1.canCollideWithLayer(hitBox2.getCollisionLayers())) return null;

        return switch (hitBox1) {
            case BoxCollider boxCollider when hitBox2 instanceof BoxCollider ->
                    twoBoxColliders(boxCollider, (BoxCollider) hitBox2);
            case BoxCollider boxCollider when hitBox2 instanceof SphereCollider ->
                    sphereAndBoxCollider(boxCollider, (SphereCollider) hitBox2, true);
            case SphereCollider sphereCollider when hitBox2 instanceof BoxCollider ->
                    sphereAndBoxCollider((BoxCollider) hitBox2, sphereCollider, false);
            case SphereCollider sphereCollider when hitBox2 instanceof SphereCollider ->
                    twoSphereColliders(sphereCollider, (SphereCollider) hitBox2);
            default -> null;
        };
    }

    private HitInfo<HitBox> twoSphereColliders(SphereCollider sphere1, SphereCollider sphere2) {
        float radius1 = sphere1.getRadius();
        float radius2 = sphere2.getRadius();
        float distance = Vector3.squaredDistance(sphere1.getPosition(), sphere2.getPosition());

        if (distance < Math.pow(radius1 + radius2, 2)) {
            sphere2.callback(sphere1);
            sphere1.callback(sphere2);
            return new HitInfo<>(sphere2.getPosition(), sphere2.getColor(), 0, sphere2);
        }

        return null;
    }

    private HitInfo<HitBox> sphereAndBoxCollider(BoxCollider box, SphereCollider sphere, boolean returnSphere) {
        float safeDistanceBox = box.getVerts()[0].subtract(box.getPosition()).squareMagnitude();
        float safeDistanceSphere = sphere.getRadius() * sphere.getRadius();

        if (Vector3.squaredDistance(box.getPosition(), sphere.getPosition()) > safeDistanceBox + safeDistanceSphere)
            return null;

        Vector3 normal = separatingAxisTest(sphere, box);
        if (normal != null) {
            if (returnSphere) {
                sphere.callback(box);
                box.callback(sphere);
                return new HitInfo<>(sphere.getPosition(), sphere.getColor(), 0, sphere);
            } else {
                box.callback(sphere);
                sphere.callback(box);
                return new HitInfo<>(box.getPosition(), box.getColor(), 0, box, normal);
            }
        }

        return null;
    }

    private HitInfo<HitBox> twoBoxColliders(BoxCollider hitBox1, BoxCollider hitBox2) {
        float safeDistance1 = hitBox1.getVerts()[0].subtract(hitBox1.getPosition()).squareMagnitude();
        float safeDistance2 = hitBox2.getVerts()[0].subtract(hitBox2.getPosition()).squareMagnitude();

        if (Vector3.squaredDistance(hitBox1.getPosition(), hitBox2.getPosition()) > safeDistance1 + safeDistance2)
            return null;

        boolean isIntersecting;
        Vector3 normal = separatingAxisTest(hitBox1, hitBox2);

        if (hitBox1.getRotation() == hitBox2.getRotation())
            isIntersecting = normal != null;
        else isIntersecting = normal != null && separatingAxisTest(hitBox2, hitBox1) != null;

        if (isIntersecting) {
            hitBox2.callback(hitBox1);
            hitBox1.callback(hitBox2);
            return new HitInfo<>(hitBox2.getPosition(), hitBox2.getColor(), 0, hitBox2, normal);
        }

        return null;
    }

    /**
     * Unless later updated, hit2 must be a BoxCollider
     * for the intersecting normal to be returned
     *
     * @return the normal of the quad it intersected with
     */
    private Vector3 separatingAxisTest(HitBox hit1, BoxCollider hit2) {
        Vector3 collisionNormal = null;
        // This is representing the quad that was intersected with
        // It's calculated using the dot product between (hit1 - hit2) and the normal
        float closestQuad = -Float.MAX_VALUE;

        if (hit1 instanceof BoxCollider box1 && hit2 instanceof BoxCollider box2) {
            for (Vector3 normal : box1.getNormals()) {
                float box1Min = Float.MAX_VALUE;
                float box1Max = -Float.MAX_VALUE;
                float box2Min = Float.MAX_VALUE;
                float box2Max = -Float.MAX_VALUE;
                normal = normal.normalize();

                for (Vector3 vertex : box1.getVerts()) {
                    box1Min = Math.min(vertex.dot(normal), box1Min);
                    box1Max = Math.max(vertex.dot(normal), box1Max);
                }

                for (Vector3 vertex : box2.getVerts()) {
                    box2Min = Math.min(vertex.dot(normal), box2Min);
                    box2Max = Math.max(vertex.dot(normal), box2Max);
                }

                if (
                        (box1Max < box2Min || box1Min > box2Max) &&
                                (box1Min < box2Min || box1Max > box2Max)
                ) return null;
            }
        } else if (hit1 instanceof SphereCollider sphere && hit2 instanceof BoxCollider box) {
            for (Vector3 normal : box.getNormals()) {
                float boxMin = Float.MAX_VALUE;
                float boxMax = -Float.MAX_VALUE;
                float sphereMin = sphere.getPosition().dot(normal) - sphere.getRadius();
                float sphereMax = sphere.getPosition().dot(normal) + sphere.getRadius();
                normal = normal.normalize();

                for (Vector3 vertex : box.getVerts()) {
                    boxMin = Math.min(vertex.dot(normal), boxMin);
                    boxMax = Math.max(vertex.dot(normal), boxMax);
                }

                if (
                        (boxMax < sphereMin || boxMin > sphereMax) &&
                                (boxMin < sphereMin || boxMax > sphereMax)
                ) return null;
            }
        }

        for (int n = 0; n < (hit2).getAllNormals().length; n++) {
            Vector3 normal = hit2.getAllNormals()[n];

            float scaleOfAxis = switch (n % 3) {
                case 0 -> hit2.getScale().x;
                case 1 -> hit2.getScale().y;
                case 2 -> hit2.getScale().z;
                default -> throw new IllegalArgumentException("Unexpected value: " + n);
            };

            float currentQuad = hit1.getPosition().subtract(hit2.getPosition()).dot(normal) / scaleOfAxis;
            if (currentQuad > closestQuad) {
                closestQuad = currentQuad;
                collisionNormal = normal;
            }
        }

        return collisionNormal;
    }

    /**
     * Tests if the raycast hit another collider.
     *
     * @return Returns "HitInfo" for information on the collided object.
     * Returns "null" if it intersects with nothing
     */
    public HitInfo<HitBox> raycastHit(Raycast raycast) {
        float previousHitDistance = Float.MAX_VALUE;
        HitInfo<HitBox> previousHit = null;

        synchronized (hitBoxes) {
            HashSet<HitBox> snapshot = new HashSet<>(hitBoxes);
            for (HitBox hitBox : snapshot) {
                HitInfo<HitBox> hit = testForRaycastCollision(hitBox, raycast, previousHitDistance);

                if (hit != null && hit.getDistance() < previousHitDistance) {
                    previousHitDistance = hit.getDistance();
                    previousHit = hit;
                }
            }
        }

        return previousHit;
    }

    /**
     * Tests if the raycast hit another collider.
     *
     * @return Returns "HitInfo" for information on the collided object.
     * Returns "null" if it intersects with nothing
     */
    public HitInfo<HitBox> raycastHit(Raycast raycast, CollisionLayer[] layer) {
        float previousHitDistance = Float.MAX_VALUE;
        HitInfo<HitBox> previousHit = null;

        synchronized (hitBoxes) {
            HashSet<HitBox> snapshot = new HashSet<>(hitBoxes);
            for (HitBox hitBox : snapshot) {
                if (!hitBox.containsCollisionLayer(layer)) continue;

                HitInfo<HitBox> hit = testForRaycastCollision(hitBox, raycast, previousHitDistance);

                if (hit != null && hit.getDistance() < previousHitDistance) {
                    previousHitDistance = hit.getDistance();
                    previousHit = hit;
                }
            }
        }

        return previousHit;
    }

    /**
     * Tests if the raycast hits anything.
     *
     * @return Returns "true" if the raycast hits anything
     */
    public boolean raycastIntersects(Raycast raycast) {
        for (HitBox hitBox : hitBoxes) {
            HitInfo<HitBox> hit = testForRaycastCollision(hitBox, raycast, Float.MAX_VALUE);
            if (hit != null) return true;
        }

        return false;
    }

    /**
     * Tests if the raycast hits anything.
     *
     * @return Returns "true" if the raycast hits anything
     */
    public boolean raycastIntersects(Raycast raycast, CollisionLayer[] layer) {
        for (HitBox hitBox : hitBoxes) {
            if (!hitBox.containsCollisionLayer(layer)) continue;

            HitInfo<HitBox> hit = testForRaycastCollision(hitBox, raycast, Float.MAX_VALUE);
            if (hit != null) return true;
        }

        return false;
    }

    private HitInfo<HitBox> testForRaycastCollision(HitBox hitBox, Raycast raycast, float previousHitDistance) {
        float nearestDistance = Float.MAX_VALUE;
        Vector3 intersection = null;
        if (
                raycast.containsRenderLayer(RenderLayer.RENDER_ONLY) &&
                        hitBox.containsRenderLayer(RenderLayer.COLLISION_ONLY)
        ) return null;

        if (hitBox instanceof BoxCollider boxCollider) {
            Vector3[][] quads = boxCollider.getQuads();
            if (quads == null) return null;

            for (Vector3[] quad : quads) {
                HitInfo<HitBox> collision = collidedWithPlane(quad, raycast, previousHitDistance);

                if (collision != null) {
                    intersection = collision.getHitPosition();
                    nearestDistance = Math.min(nearestDistance, collision.getDistance());
                }
            }

            return new HitInfo<>(intersection, boxCollider.getColor(), nearestDistance, boxCollider);
        } else if (hitBox instanceof SphereCollider sphere) {
            // L is defined as the sphere center pointing to the raycast (P - C)
            Vector3 L = raycast.position.subtract(sphere.getPosition());
            Vector3 D = raycast.getDirection();
            float r = sphere.getRadius();

            if (D.dot(D) == 0) return null;

            float discriminant = 4 * D.dot(L) * D.dot(L) - 4 * D.dot(D) * (L.dot(L) - r * r);

            if (discriminant < 0) return null;

            // t is the distance to the intersection
            float t_pos = (float) (-2 * D.dot(L) + Math.sqrt(discriminant)) / 2 * D.dot(D);
            float t_neg = (float) (-2 * D.dot(L) - Math.sqrt(discriminant)) / 2 * D.dot(D);

            if (t_pos < 0 && t_neg < 0) return null;

            Vector3 inter_pos = raycast.position.add(raycast.getDirection().scale(t_pos));
            Vector3 inter_neg = raycast.position.add(raycast.getDirection().scale(t_neg));

            if (inter_pos.squareMagnitude() < inter_neg.squareMagnitude() && t_pos > 0) {
                intersection = inter_pos;
                nearestDistance = t_pos;
            } else if (inter_neg.squareMagnitude() < inter_pos.squareMagnitude() && t_neg > 0) {
                intersection = inter_neg;
                nearestDistance = t_neg;
            } else return null;

            return new HitInfo<>(intersection, sphere.getColor(), nearestDistance, sphere);
        } else if (hitBox instanceof SpriteCollider sprite) {
            HitInfo<HitBox> collision = collidedWithPlane(sprite.getVerts(), raycast, previousHitDistance);

            if (collision != null) {
                intersection = collision.getHitPosition();

                Vector3 intersectionOnPlane = intersection.subtract(sprite.getPosition()).scale(2.0f);
                int sign = sprite.getRight().dot(intersectionOnPlane) >= 0 ? 1 : -1;
                float xSquared = intersectionOnPlane.x * intersectionOnPlane.x;
                float zSquared = intersectionOnPlane.z * intersectionOnPlane.z;
                intersectionOnPlane.x = sign * (float) Math.sqrt(xSquared + zSquared) / sprite.getScale().x;
                intersectionOnPlane.y /= sprite.getScale().y;

                Color color = sprite.getColor(intersectionOnPlane.x, intersectionOnPlane.y);

                if (color == null)
                    return null;

                if (color.getConsoleColor() == Console.TextColor.DEFAULT && !ColorService.FULL_ANSI_COLOR_SUPPORT)
                    return null;

                return new HitInfo<>(intersection, color, collision.getDistance(), sprite);
            }
        }

        return null;
    }

    private HitInfo<HitBox> collidedWithPlane(Vector3[] verts, Raycast raycast, float previousHitDistance) {
        // Part of the logic came from ChatGPT-4o and Grok3
        // Modified and corrected by me

        if (verts == null) return null;

        // Points on one of the rectangle's planes
        Vector3 a = verts[0];
        Vector3 b = verts[1];
        Vector3 c = verts[2];
        Vector3 d = verts[3];

        if (a == null || b == null || c == null || d == null) return null;
        Vector3 normal = b.subtract(a).cross(c.subtract(a));

        if (Math.abs(raycast.getDirection().dot(normal)) < 1e-6)
            return null;

        // t is from the equation: (P + tD - C) dot n = 0
        float t = -((raycast.position.subtract(a)).dot(normal))
                / raycast.getDirection().dot(normal);

        if (t > raycast.DISTANCE || t < 0 || t > previousHitDistance)
            return null;

        Vector3 intersection = raycast.position.add(raycast.getDirection().scale(t));

        // Edge Vectors
        Vector3 edge1 = a.subtract(b);
        Vector3 edge2 = b.subtract(c);
        Vector3 edge3 = c.subtract(d);
        Vector3 edge4 = d.subtract(a);

        Vector3 c1 = intersection.subtract(a).cross(edge1);
        Vector3 c2 = intersection.subtract(b).cross(edge2);
        Vector3 c3 = intersection.subtract(c).cross(edge3);
        Vector3 c4 = intersection.subtract(d).cross(edge4);

        boolean inside = (c1.dot(normal) >= 0) && (c2.dot(normal) >= 0) &&
                (c3.dot(normal) >= 0) && (c4.dot(normal) >= 0);

        if (inside)
            return new HitInfo<>(intersection, new Color(Console.TextColor.DEFAULT), t, null);

        return null;
    }

    public void addHitBox(HitBox hitBox) {
        synchronized (hitBoxes) {
            hitBoxes.add(hitBox);
        }
    }

    public void removeHitBox(HitBox hitBox) {
        synchronized (hitBoxes) {
            hitBoxes.remove(hitBox);
        }
    }

    /**
     * Returns all HitBoxes, ignoring the quadtree
     */
    public Set<HitBox> getHitBoxes() {
        synchronized (hitBoxes) {
            return hitBoxes;
        }
    }

    /**
     * Returns all relevant HitBoxes by utilizing a Quadtree
     */
    public Set<HitBox> getHitBoxes(HitBox hitBox) {
        synchronized (hitBoxes) {
            rebuildQuadtree();
            List<HitBox> hitBoxesInTree = new ArrayList<>();
            quadtree.retrieve(hitBoxesInTree, hitBox);
            return new LinkedHashSet<>(hitBoxesInTree);
        }
    }

    private void rebuildQuadtree() {
        quadtree.clear();
        quadtree.insertAll(hitBoxes.toArray(new HitBox[0]));
    }
}
