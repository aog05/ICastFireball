package edu.neumont.csc150.model.misc;

import edu.neumont.csc150.model.enums.CollisionLayer;
import edu.neumont.csc150.model.enums.RenderLayer;
import edu.neumont.csc150.model.misc.raycast.Raycast;
import edu.neumont.csc150.service.ConfigService;
import edu.neumont.csc150.service.Injectable;

public class Camera {
    private final float FOV = 90.0f;
    private final float FOV_RAD = (float) Math.toRadians(FOV);
    private final float ASPECT_RATIO;

    private Vector3 position = Vector3.zero();
    /** In radians */
    private float rotation;
    public Raycast[][] rays;
    private final ConfigService configService;

    public Camera(Injectable config) {
        configService = (ConfigService) config;
        rays = new Raycast[configService.screenHeight][configService.screenWidth];
        ASPECT_RATIO = (float) configService.screenWidth / (3 * configService.screenHeight);

        calculateRays(true);
    }

    public Vector3 forward() {
        float x = (float) Math.sin(rotation);
        float z = (float) Math.cos(rotation);
        return new Vector3(x, 0, -z);
    }

    private void calculateRays(boolean initializeRays) {
        for (int x = 0; x < rays.length; x++) {
            float lerpX = (float) x / (rays.length - 1);
            float dirX = ((1 - lerpX) * -FOV_RAD / 2 + lerpX * FOV_RAD / 2);

            for (int y = 0; y < rays[x].length; y++) {
                float lerpY = (float) y / (rays[x].length - 1);
                float dirY = (1 - lerpY) * -FOV_RAD / 2 + lerpY * FOV_RAD / 2;

                Vector3 offset = new Vector3((float) -Math.sin(dirY), (float) -Math.sin(dirX), 1);
                Vector3 direction = new Vector3(
                        (float) (ASPECT_RATIO * -offset.x * Math.cos(rotation) + offset.z * Math.sin(rotation)),
                        offset.y,
                        (float) (-offset.x * Math.sin(rotation) - offset.z * Math.cos(rotation))
                );

                if (initializeRays) {
                    rays[x][y] = new Raycast(
                            position,
                            direction,
                            32,
                            new RenderLayer[]{ RenderLayer.RENDER_ONLY },
                            new CollisionLayer[]{}
                    );
                } else {
                    rays[x][y].position = position;
                    rays[x][y].setDirection(direction);
                }
            }
        }
    }

    public Vector3 getPosition() {
        return new Vector3(position);
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        calculateRays(false);
    }

    public float getRotation() {
        return (float) Math.toDegrees(rotation);
    }

    /** Set rotation in degrees */
    public void setRotation(float rotation) {
        this.rotation = (float) Math.toRadians(rotation);
        calculateRays(false);
    }
}
