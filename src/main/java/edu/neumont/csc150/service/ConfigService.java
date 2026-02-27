package edu.neumont.csc150.service;

public class ConfigService implements Injectable {
    public final int framesPerSecond;
    public final int screenWidth;
    public final int screenHeight;
    public final float brightness;
    public final float fixedDeltaTime;
    public final byte sceneWorkerThreadCount;
    private static float deltaTime;
    private long previousFrameMs = System.currentTimeMillis();

    public ConfigService() {
        framesPerSecond = 30;
        screenWidth = 160;
        screenHeight = 40;
        brightness = 1.35f;
        fixedDeltaTime = 1.0f / framesPerSecond;
        sceneWorkerThreadCount = 16;
        deltaTime = fixedDeltaTime;
    }

    @Override
    public void startService() {}

    @Override
    public void stopService() {}

    public void updateDeltaTime() {
        deltaTime = 1.0f / (System.currentTimeMillis() - previousFrameMs);
        previousFrameMs = System.currentTimeMillis();
    }

    public static float getDeltaTime() {
        return deltaTime;
    }
}
