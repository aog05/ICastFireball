package edu.neumont.csc150.controller;

import edu.neumont.csc150.model.Muffin;
import edu.neumont.csc150.model.Player;
import edu.neumont.csc150.model.colliders.HitBox;
import edu.neumont.csc150.model.enums.SceneType;
import edu.neumont.csc150.model.goblins.Goblin;
import edu.neumont.csc150.model.misc.AudioPlayer;
import edu.neumont.csc150.model.misc.Camera;
import edu.neumont.csc150.model.misc.SceneWrapper;
import edu.neumont.csc150.model.misc.Vector3;
import edu.neumont.csc150.model.projectiles.ProjectileManager;
import edu.neumont.csc150.service.*;
import javafx.scene.input.KeyCode;

import java.util.LinkedHashSet;
import java.util.LinkedList;

public final class GameController implements OnSceneChange {
    private Thread gameLoop;
    private final UserInput inputService;
    private final ConfigService configService;
    private final CollisionService collisionService;
    private final ProjectileManager projectileManager;
    private final SceneService sceneService;
    private final SoundService soundService;
    private AudioPlayer music;
    private volatile static Player player;
    public volatile static Camera camera;
    private final SceneWrapper sceneWrapper = new SceneWrapper();
    private final AudioPlayer reviveSound;

    public GameController(
            Injectable userInput,
            Injectable config,
            Injectable collision,
            Injectable scene,
            Injectable sound
    ) {
        inputService = (UserInput) userInput;
        configService = (ConfigService) config;
        collisionService = (CollisionService) collision;
        projectileManager = new ProjectileManager(collision, config);
        sceneService = (SceneService) scene;
        soundService = (SoundService) sound;

        sceneService.addOnSceneChange(this);
        inputService.lockMouse = true;
        reviveSound = soundService.create(
                "assets/audio/sound effects/Revive.wav",
                false
        );
    }

    public void start() {
        if (music != null) music.stop();

        switch (sceneWrapper.scene) {
            case TITLE, INTERMISSION, FIREBALL_CUTSCENE -> {
            }
            case INTRO_CUTSCENE -> startIntroCutscene();
            case FIRST_FLOOR, SECOND_FLOOR, GALLERY, BOSS_ROOM -> startGame(sceneWrapper.scene);
            case GAME_OVER -> startGameOver();
            default -> throw new IllegalStateException("Unexpected value: " + sceneWrapper.scene);
        }
    }

    public void initializeLoop() {
        gameLoop = new Thread(() -> {
            while (!inputService.getKeyDown().contains(KeyCode.ESCAPE)) {
                synchronized (this) {
                    update(sceneWrapper);

                    try {
                        this.wait(1000 / configService.framesPerSecond);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            }

            exit();
        });

        gameLoop.start();
    }

    private void update(SceneWrapper wrapper) {
        switch (wrapper.scene) {
            case TITLE -> updateTitleScreen();
            case INTRO_CUTSCENE -> updateIntroCutscene();
            case FIRST_FLOOR, SECOND_FLOOR, GALLERY, BOSS_ROOM -> updateGame();
            case GAME_OVER -> updateGameOver();
            case INTERMISSION -> updateIntermission();
            case FIREBALL_CUTSCENE -> {
            }
            default -> throw new IllegalStateException("Unexpected value: " + sceneWrapper.scene);
        }
    }

    // region Title Screen
    private void updateTitleScreen() {
        if (inputService.getKeyPressed() == KeyCode.ENTER.getChar().charAt(0))
            sceneService.changeScene(SceneType.INTRO_CUTSCENE);
    }
    // endregion

    // region Intro Cutscene
    private void startIntroCutscene() {
        music = soundService.create("assets/audio/I Cast Fireball Intro.wav", false);
        music.play();
    }

    private void updateIntroCutscene() {
        if (inputService.getKeyPressed() == KeyCode.ENTER.getChar().charAt(0))
            sceneService.changeScene(SceneType.INTERMISSION);
    }
    // endregion

    // region Game
    private void startGame(SceneType scene) {
        if (player == null) {
            camera = new Camera(configService);
            player = new Player(inputService, configService, collisionService, camera, sceneService, soundService);
        }
        player.setPosition(Vector3.zero());
        player.rotation = 180;

        switch (scene) {
            case FIRST_FLOOR, SECOND_FLOOR -> {
                music = soundService.create("assets/audio/The Tower.wav", true);
                music.play();
            }
            case BOSS_ROOM -> {
                music = soundService.create("assets/audio/A Knoble Knight.wav", true);
                music.play();
            }
        }

        Map.loadLevel(scene);
    }

    private void updateGame() {
        if (player == null) return;

        player.update();
        LinkedList<Goblin> goblinSnapshot = new LinkedList<>(Map.getGoblins());
        for (Goblin goblin : goblinSnapshot) {
            if (player == null) break;
            goblin.update(player.getPosition());
        }

        LinkedHashSet<Muffin> muffinSnapshot = new LinkedHashSet<>(Map.getMuffins());
        for (Muffin muffin : muffinSnapshot) {
            if (player == null) break;
            muffin.update(player.getPosition());
        }

        for (HitBox trigger : Map.getTriggerColliders())
            trigger.getCollision(player.getCollider());

        if (Map.getSirHoly() != null)
            Map.getSirHoly().update(player.getPosition());

        projectileManager.update();

        configService.updateDeltaTime();
    }

    public static Player getPlayer() {
        return player;
    }
    // endregion

    // region Game Over
    private void startGameOver() {
        music = soundService.create("assets/audio/Game Over.wav", true);
        music.play();
        player = null;
        camera = null;
    }

    private void updateGameOver() {
        if (inputService.getKeyPressed() == KeyCode.ENTER.getChar().charAt(0))
            new Thread(() -> {
                synchronized (this) {
                    music.stop();
                    reviveSound.play();

                    try {
                        this.wait(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    sceneService.changeScene(SceneType.FIRST_FLOOR);
                }
            }).start();
    }
    //endregion

    // region Intermission
    private void updateIntermission() {
        if (inputService.getKeyPressed() == KeyCode.ENTER.getChar().charAt(0)) {
            SceneType changeTo = switch (sceneService.getPreviousScene()) {
                case INTRO_CUTSCENE -> SceneType.FIRST_FLOOR;
                case FIRST_FLOOR -> SceneType.SECOND_FLOOR;
                case SECOND_FLOOR -> SceneType.GALLERY;
                case GALLERY -> SceneType.BOSS_ROOM;
                default -> throw new IllegalStateException("Unexpected value: " + sceneService.getPreviousScene());
            };

            sceneService.changeScene(changeTo);
        }
    }
    // endregion

    @Override
    public void sceneChangeEvent(SceneType scene) {
        sceneWrapper.scene = scene;
        Map.clearLevel();
        start();
    }

    private void exit() {
        gameLoop.interrupt();
        InitController.exit();
    }
}
