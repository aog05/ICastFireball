package edu.neumont.csc150.controller;

import edu.neumont.csc150.model.enums.SceneType;
import edu.neumont.csc150.model.misc.AudioPlayer;
import edu.neumont.csc150.model.misc.Camera;
import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.model.misc.SceneWrapper;
import edu.neumont.csc150.service.*;
import edu.neumont.csc150.view.Console;
import edu.neumont.csc150.view.Overlay;
import edu.neumont.csc150.view.SceneRenderer;
import javafx.scene.input.KeyCode;

import java.util.Arrays;

public final class RenderController implements OnSceneChange {
    private Thread renderThread;
    private final SceneRenderer renderer;
    private final Overlay overlay;
    private final ConfigService configService;
    private final SceneService sceneService;
    private final UserInput inputService;
    private final SoundService soundService;
    private boolean shutdownThread = false;
    private final SceneWrapper sceneWrapper = new SceneWrapper();
    public static boolean[] workersFinished;

    // region Cutscene
    private Thread cutsceneThread;
    private String[] cutsceneImages;
    private volatile String cutsceneImageDisplayed;
    private volatile String cutsceneText;
    private float imageYPosition;
    private float imageXPosition;
    private float imageYSpeed;
    private int textXPosition;
    private int textYPosition;
    private Color fillColor;

    private final AudioPlayer youThoughtYouCouldBeatMe;
    private final AudioPlayer iCastFireball;
    private final AudioPlayer explosionSound;
    // endregion

    public RenderController(
            Injectable config,
            Injectable color,
            Injectable collision,
            Injectable scene,
            Injectable input,
            Injectable sound
    ) {
        GameController.camera = new Camera(config);
        configService = (ConfigService) config;
        sceneService = (SceneService) scene;
        inputService = (UserInput) input;
        soundService = (SoundService) sound;
        sceneService.addOnSceneChange(this);
        renderer = new SceneRenderer(configService.screenWidth, configService.screenHeight, color, collision, config);
        overlay = new Overlay(color, config);
        workersFinished = new boolean[configService.sceneWorkerThreadCount];

        youThoughtYouCouldBeatMe = soundService.create(
                "assets/audio/end cutscene/You Thought You Could Beat Me.wav",
                false
        );

        iCastFireball = soundService.create(
                "assets/audio/end cutscene/I Cast Fireball.wav",
                false
        );

        explosionSound = soundService.create(
                "assets/audio/end cutscene/Explosion.wav",
                false
        );
    }

    public void startRendering() {
        renderThread = new Thread(() -> {
            synchronized (this) {
                while (!shutdownThread) {
                    if (frameRendered()) {
                        drawOverlay(sceneWrapper);
                        renderer.endDrawing();
                        renderer.clearBackground(new Color(Console.TextColor.WHITE), (byte) 0);
                        Arrays.fill(workersFinished, false);
                    }

                    try {
                        this.wait(1000 / configService.framesPerSecond);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        renderer.initializeWorkerThreads(workersFinished);
        renderThread.start();
    }

    @Override
    public void sceneChangeEvent(SceneType scene) {
        renderer.clearBackground(new Color(Console.TextColor.WHITE), (byte) 0);
        renderer.endDrawing();
        sceneWrapper.scene = scene;

        if (scene == SceneType.INTRO_CUTSCENE) {
            cutsceneImages = new String[]{
                    "file:assets/images/intro/Intro 1.png",
                    "file:assets/images/intro/Intro 2.png",
                    "file:assets/images/intro/Intro 3.png",
                    "file:assets/images/intro/Intro 4.png"
            };
            cutsceneImageDisplayed = cutsceneImages[0];
            cutsceneText = "The flowers, trees,\nand that sea breeze.";

            startIntroCutscene();
            return;
        } else stopCutscene();

        if (scene == SceneType.GAME_OVER) {
            cutsceneImages = new String[]{
                    "file:assets/images/game over/Game Over 1.png",
                    "file:assets/images/game over/Game Over 2.png"
            };

            imageXPosition = 0.5f;
            imageYPosition = 0.5f;
            textXPosition = configService.screenWidth / 2 - 15;
            textYPosition = configService.screenHeight - 2;
            imageYSpeed = -0.1f;
            cutsceneText = "'Enter' to Reclaim the Muffins";
            cutsceneImageDisplayed = cutsceneImages[0];
            return;
        }

        if (scene == SceneType.FIREBALL_CUTSCENE) {
            cutsceneImages = new String[]{
                    "file:assets/images/ending/End Cutscene Knight.png",
                    "file:assets/images/ending/End Cutscene Wizard1.png",
                    "file:assets/images/ending/End Cutscene Wizard2.png",
                    "file:assets/images/ending/End Cutscene Wizard3.png"
            };

            startFireballCutscene();
        } else stopCutscene();
    }

    public void stopRendering() {
        shutdownThread = true;
        renderer.shutdownPool();
    }

    private void drawOverlay(SceneWrapper wrapper) {
        switch (wrapper.scene) {
            case TITLE -> {
                overlay.titleScreen();
                overlay.displayText(
                        configService.screenWidth / 2 - 8,
                        configService.screenHeight - 2,
                        "'Enter' to Start"
                );
            }
            case INTRO_CUTSCENE, FIREBALL_CUTSCENE -> {
                overlay.displayImage(imageXPosition, imageYPosition, 2, 1, cutsceneImageDisplayed);
                overlay.displayText(textXPosition, textYPosition, cutsceneText);
                imageYPosition += imageYSpeed * ConfigService.getDeltaTime();
            }
            case FIRST_FLOOR, SECOND_FLOOR, GALLERY, BOSS_ROOM -> {
                if (GameController.getPlayer() == null) break;
                overlay.drawHealth(0.25f, 0.75f, GameController.getPlayer().remainingHealth());
                overlay.displayImage(0.5f, 0.5f, 2, 1, GameController.getPlayer().getHandState());
            }
            case GAME_OVER -> {
                overlay.displayImage(imageXPosition, imageYPosition, 2, 1, cutsceneImageDisplayed);
                overlay.displayText(textXPosition, textYPosition, cutsceneText);
                if (inputService.getKeyDown().contains(KeyCode.ENTER))
                    cutsceneImageDisplayed = cutsceneImages[1];
            }
            case INTERMISSION -> {
                textYPosition = configService.screenHeight / 2 - 1;

                switch (sceneService.getPreviousScene()) {
                    case INTRO_CUTSCENE -> {
                        textXPosition = configService.screenWidth / 2 - 12;
                        cutsceneText = "First Floor of the Castle\n\n'Enter' to Start";
                    }
                    case FIRST_FLOOR -> {
                        textXPosition = configService.screenWidth / 2 - 13;
                        cutsceneText = "Second Floor of the Castle\n\n'Enter' to Start";
                    }
                    case SECOND_FLOOR -> {
                        textXPosition = configService.screenWidth / 2 - 16;
                        cutsceneText = "The Castle's Prestigious Gallery\n\n'Enter' to Observe";
                    }
                    case GALLERY -> {
                        textXPosition = configService.screenWidth / 2 - 15;
                        cutsceneText = "The Final Battle\n\n'Enter' to Reclaim the Muffins";
                    }
                }

                overlay.displayText(textXPosition, textYPosition, cutsceneText);
            }
            default -> throw new IllegalStateException("Unexpected value: " + wrapper.scene);
        }

        if (fillColor != null)
            renderer.clearBackground(fillColor, (byte) 12);
    }

    // region Intro Cutscene
    private void startIntroCutscene() {
        cutsceneThread = new Thread(() -> {
            synchronized (this) {
                try {
                    imageXPosition = 0.4f;
                    imageYPosition = -0.25f;
                    imageYSpeed = 0.1f;
                    textXPosition = configService.screenWidth - 25;
                    textYPosition = configService.screenHeight / 2;
                    cutsceneImageDisplayed = cutsceneImages[0];

                    this.wait(8000);

                    cutsceneText = "Another magical,\npeaceful day in Wizcomb.";

                    this.wait(8000);

                    imageXPosition = 0.4f;
                    imageYPosition = 1.5f;
                    imageYSpeed = -0.2f;
                    cutsceneImageDisplayed = cutsceneImages[1];
                    cutsceneText = "A small snack won't\nhurt my wizarding duties.";

                    this.wait(8000);

                    textXPosition = configService.screenWidth - 30;
                    cutsceneText = "I have some splendid muffins!\nThanks to my muffinnomicon.";

                    this.wait(5000);

                    imageXPosition = 0.5f;
                    imageYPosition = 0.5f;
                    imageYSpeed = 0;
                    cutsceneImageDisplayed = cutsceneImages[2];
                    cutsceneText = "NOOOO! WHAT HAPPENED TO MY MUFFINS!!\nIT MUST BE THOSE GOBLINS.";
                    textXPosition = configService.screenWidth / 2 - 18;
                    textYPosition = configService.screenHeight - 5;

                    this.wait(7000);

                    imageXPosition = 0.5f;
                    imageYPosition = 0.5f;
                    imageYSpeed = 0;
                    cutsceneImageDisplayed = cutsceneImages[3];
                    cutsceneText = "Unacceptable, I say.";
                    textXPosition = configService.screenWidth - 79;
                    textYPosition = configService.screenHeight - 27;

                    this.wait(8000);

                    sceneService.changeScene(SceneType.INTERMISSION);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        cutsceneThread.start();
    }
    // endregion

    // region Fireball Cutscene
    private void startFireballCutscene() {
        imageXPosition = 0.5f;
        imageYPosition = 0.5f;
        imageYSpeed = 0;
        textXPosition = configService.screenWidth / 2;
        textYPosition = configService.screenHeight / 2;
        cutsceneImageDisplayed = cutsceneImages[0];
        cutsceneText = "";

        cutsceneThread = new Thread(() -> {
            synchronized (this) {
                try {
                    this.wait(2000);
                    youThoughtYouCouldBeatMe.play();
                    this.wait(3000);
                    cutsceneImageDisplayed = cutsceneImages[1];
                    this.wait(2000);
                    cutsceneImageDisplayed = cutsceneImages[2];
                    iCastFireball.play();
                    this.wait(1800);
                    cutsceneImageDisplayed = cutsceneImages[3];
                    this.wait(2500);
                    explosionSound.play();
                    fillColor = new Color(255, 255, 255);

                    this.wait(3000);
                    cutsceneImageDisplayed = null;
                    for (int i = 1; i < 10; i++) {
                        fillColor = new Color(255 / i, 255 / i, 255 / i);
                        this.wait(250);
                    }

                    fillColor = null;
                    cutsceneText = "Fin.";
                    textXPosition = configService.screenWidth / 2 - 2;

                    this.wait(5000);
                    cutsceneText = "Game made by:\n\nArthur Grover\n\njellycake222";
                    textXPosition = configService.screenWidth / 2 - 6;
                } catch (InterruptedException _) {}
            }
        });

        cutsceneThread.start();
    }
    // endregion

    private void stopCutscene() {
        if (cutsceneThread != null && !cutsceneThread.isInterrupted())
            cutsceneThread.interrupt();
    }

    private boolean frameRendered() {
        for (boolean finished : workersFinished) {
            if (!finished) return false;
        }

        return true;
    }
}
