package edu.neumont.csc150.model.misc;

import edu.neumont.csc150.model.colliders.BoxCollider;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.colliders.SphereCollider;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Idea and code came from Steven Lambert
 * <br>
 * <a href="https://code.tutsplus.com/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374t">
 *     Quick Tip: Use Quadtrees to Detect Likely Collisions in 2D Space
 * </a> */
public class Quadtree {
    private final int MAX_OBJECTS = 10;
    private final int MAX_LEVELS = 5;

    private Rectangle bounds;
    private int level;
    private List<HitBox> objects;
    private Quadtree[] nodes;

    public Quadtree(int level, Rectangle bounds) {
        this.level = level;
        this.bounds = bounds;
        this.objects = new ArrayList<>();
        this.nodes = new Quadtree[4];
    }

    /** Clear this node and all nodes beneath it */
    public void clear() {
        objects.clear();

        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] == null) continue;

            nodes[i].clear();
            nodes[i] = null;
        }
    }

    /** Split the tree when there are more than MAX_OBJECTS amount in the current node */
    public void split() {
        int subWidth = (int) bounds.getWidth() / 2;
        int subHeight = (int) bounds.getHeight() / 2;
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();

        nodes[0] = new Quadtree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new Quadtree(level + 1, new Rectangle(x, y, subWidth, subHeight));
        nodes[2] = new Quadtree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new Quadtree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    /** Get the index of the quadrant that the HitBox falls into
     * @return The index of the HitBox's quadrant */
    public int getIndex(HitBox hitBox) {
        Rectangle rect = toRectangle(hitBox);

        if (rect == null) return -1;

        int index = -1;
        double verticalMidpoint = bounds.getX() + bounds.getWidth() / 2;
        double horizontalMidpoint = bounds.getY() + bounds.getHeight() / 2;
        boolean topQuadrant = (rect.getY() < horizontalMidpoint && rect.getY() + rect.getHeight() < horizontalMidpoint);
        boolean bottomQuadrant = (rect.getY() > horizontalMidpoint);

        if (rect.getX() < verticalMidpoint && rect.getX() + rect.getWidth() < verticalMidpoint) {
            if (topQuadrant) index = 1;
            else if (bottomQuadrant) index = 2;
        } else if (rect.getX() > verticalMidpoint) {
            if (topQuadrant) index = 0;
            else if (bottomQuadrant) index = 3;
        }

        return index;
    }

    /** Insert a HitBox */
    public void insert(HitBox hitBox) {
        if (nodes[0] != null) {
            int index = getIndex(hitBox);
            if (index != -1) {
                nodes[index].insert(hitBox);
                return;
            }
        }

        objects.add(hitBox);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) split();

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                    break;
                }
                else i++;
            }
        }
    }

    /** Insert an array of HitBoxes into the quadtree */
    public void insertAll(HitBox[] hitBoxes) {
        for (HitBox hitBox : hitBoxes)
            insert(hitBox);
    }

    /**
     * Go through all the possible options of HitBoxes (foundObjects) and
     * trim down the selection to find the least amount of collision checks as possible.
     */
    public void retrieve(List<HitBox> foundObjects, HitBox hitBox) {
        int index = getIndex(hitBox);
        if (nodes[0] != null && index != -1)
            nodes[index].retrieve(foundObjects, hitBox);

        foundObjects.addAll(objects);
    }

    private Rectangle toRectangle(HitBox hitBox) {
        if (hitBox instanceof BoxCollider boxCollider) {
            float minX = Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float maxZ = -Float.MAX_VALUE;

            // The sixth quad is the top face of the box
            for (Vector3 vert : boxCollider.getQuads()[5]) {
                minX = Math.min(vert.x, minX);
                minZ = Math.min(vert.z, minZ);
                maxX = Math.max(vert.x, maxX);
                maxZ = Math.max(vert.z, maxZ);
            }

            return new Rectangle((int) minX, (int) minZ, (int) maxX, (int) maxZ);
        } else if (hitBox instanceof SphereCollider sphereCollider) {
            float radius = sphereCollider.getRadius();
            Vector3 pos = sphereCollider.getPosition();

            return new Rectangle((int) (pos.x - radius), (int) (pos.z - radius), (int) radius, (int) radius);
        }

        return null;
    }
}
