package edu.neumont.csc150.model.misc;

public class Vector3 {
    public float x;
    public float y;
    public float z;

    public Vector3() {}

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x + v.x, y + v.y, z + v.z);
    }

    public Vector3 subtract(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public Vector3 scale(float factor) {
        return new Vector3(x * factor, y * factor, z * factor);
    }

    public float dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector3 cross(Vector3 v) {
        Vector3 cross = new Vector3();
        cross.x = y * v.z - z * v.y;
        cross.y = z * v.x - x * v.z;
        cross.z = x * v.y - y * v.x;
        return cross;
    }

    public float squareMagnitude() {
        return x * x + y * y + z * z;
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3 normalize() {
        Vector3 normalized = new Vector3(this);
        float mag = magnitude();
        normalized.x /= mag;
        normalized.y /= mag;
        normalized.z /= mag;
        return normalized;
    }

    public static Vector3 zero() {
        return new Vector3(0, 0, 0);
    }

    public static float squaredDistance(Vector3 v1, Vector3 v2) {
        float dx = v2.x - v1.x;
        float dy = v2.y - v1.y;
        float dz = v2.z - v1.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static float distance(Vector3 v1, Vector3 v2) {
        return (float) Math.sqrt(squaredDistance(v1, v2));
    }
}
