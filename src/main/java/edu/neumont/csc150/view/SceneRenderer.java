package edu.neumont.csc150.view;

import edu.neumont.csc150.controller.GameController;
import edu.neumont.csc150.controller.RenderController;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.model.misc.raycast.*;
import edu.neumont.csc150.service.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.Writer;

public class SceneRenderer extends UITask {
    private final Terminal terminal;
    private final Writer writer;

    private final CollisionService collisionService;
    private final ConfigService configService;
    private final Thread[] workerThreads;

    public SceneRenderer(int screenWidth, int screenHeight, Injectable color, Injectable collision, Injectable config) {
        super(color);

        try {
            terminal = TerminalBuilder.builder().system(true).build();
            writer = terminal.writer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        frameBuffer = new long[screenHeight][screenWidth];
        collisionService = (CollisionService) collision;
        configService = (ConfigService) config;
        workerThreads = new Thread[configService.sceneWorkerThreadCount];
    }

    public void initializeWorkerThreads(Object syncTo) {
        if (configService.screenWidth % configService.sceneWorkerThreadCount != 0)
            throw new RuntimeException("The screen's width must be divisible by the amount of worker threads.\n" +
                    "Screen Width: " + configService.screenWidth + "\tWorkers: " + configService.sceneWorkerThreadCount);

        int threadCount = configService.sceneWorkerThreadCount;
        int colsToRender = configService.screenWidth / threadCount;

        for (int t = 0; t < threadCount; t++) {
            int finalT = t;
            int startCoordinate = colsToRender * t;
            int endCoordinate = colsToRender * (t + 1);

            workerThreads[t] = new Thread(() -> {
                while (true) {
                    synchronized (syncTo) {
                        if (RenderController.workersFinished[finalT]) continue;

                        synchronized (collisionService.getHitBoxes()) {
                            for (int x = 0; x < frameBuffer.length; x++) {
                                for (int y = startCoordinate; y < endCoordinate; y++) {
                                    if (GameController.camera == null) break;
                                    HitInfo<HitBox> hit = collisionService.raycastHit(GameController.camera.rays[x][y]);
                                    byte brightness;
                                    Color color;

                                    if (hit != null) {
                                        brightness = colorService.getBrightnessFromDistance(hit.getDistance(), configService.brightness);
                                        color = hit.getColor();
                                    } else continue;

                                    frameBuffer[x][y] = colorService.getColoredBrightness(color, brightness);
                                }
                            }
                        }

                        RenderController.workersFinished[finalT] = true;
                    }
                }
            });

            workerThreads[t].start();
        }
    }

    public void clearBackground(Color color, byte brightness) {
        for (int x = 0; x < frameBuffer.length; x++) {
            for (int y = 0; y < frameBuffer[x].length; y++) {
                frameBuffer[x][y] = colorService.getColoredBrightness(color, brightness);
            }
        }
    }

    public void endDrawing() {
        StringBuilder sb = new StringBuilder(frameBuffer.length * frameBuffer[0].length * 3);
        long previousColor = -1;

        for (int x = 0; x < frameBuffer.length; x++) {
            for (int y = 0; y < frameBuffer[x].length; y++) {
                char character = colorService.getUncoloredCharacter(frameBuffer[x][y]);

                if (ColorService.FULL_ANSI_COLOR_SUPPORT) {
                    long color = colorService.getANSIColor(frameBuffer[x][y]);
                    if (previousColor != color) {
                        sb.append(colorService.getANSIColorString(color));
                        previousColor = color;
                    }
                } else {
                    Color color = colorService.getColor(frameBuffer[x][y]);
                    if (previousColor != color.toLong()) {
                        sb.append(colorService.getRawColorCode(color.getConsoleColor()));
                        previousColor = color.toLong();
                    }
                }

                sb.append(character);

                if (y == frameBuffer[x].length - 1)
                    sb.append('\n');
            }
        }

        try {
            writer.write("\033[H");
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdownPool() {
        for (Thread worker : workerThreads)
            worker.interrupt();

        try {
            terminal.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
