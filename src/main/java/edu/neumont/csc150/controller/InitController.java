package edu.neumont.csc150.controller;

import edu.neumont.csc150.model.enums.SceneType;
import edu.neumont.csc150.service.*;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class InitController {
    private static GameController gameController;
    private static RenderController renderController;

    private enum InjectableTypes {
        USER_INPUT,
        CONFIG,
        COLOR,
        COLLISION,
        SCENE,
        SOUND
    }

    private static Scene scene;

    private static Injectable[] injectables;

    private InitController() {
        throw new IllegalStateException("Utility class");
    }

    public static void init(Stage stage) {
        stage.setTitle("Input Window (Focus on Me!)");
        StackPane root = new StackPane();
        scene = new Scene(root, 350, 200);
        stage.setX(Screen.getPrimary().getVisualBounds().getMaxX() - scene.getWidth());
        stage.setY(Screen.getPrimary().getVisualBounds().getMaxY() - scene.getHeight() - 50);
        stage.setScene(scene);
        stage.show();

        injectables = new Injectable[]{
                new UserInput(scene),
                new ConfigService(),
                new ColorService(),
                new CollisionService(),
                new SceneService(),
                new SoundService()
        };

        for (Injectable injectable : injectables)
            injectable.startService();

        gameController = new GameController(
                injectables[InjectableTypes.USER_INPUT.ordinal()],
                injectables[InjectableTypes.CONFIG.ordinal()],
                injectables[InjectableTypes.COLLISION.ordinal()],
                injectables[InjectableTypes.SCENE.ordinal()],
                injectables[InjectableTypes.SOUND.ordinal()]
        );

        renderController = new RenderController(
                injectables[InjectableTypes.CONFIG.ordinal()],
                injectables[InjectableTypes.COLOR.ordinal()],
                injectables[InjectableTypes.COLLISION.ordinal()],
                injectables[InjectableTypes.SCENE.ordinal()],
                injectables[InjectableTypes.USER_INPUT.ordinal()],
                injectables[InjectableTypes.SOUND.ordinal()]
        );

        Map.injectCollisionService(injectables[InjectableTypes.COLLISION.ordinal()]);
        Map.injectColorService(injectables[InjectableTypes.COLOR.ordinal()]);
        Map.injectConfigService(injectables[InjectableTypes.CONFIG.ordinal()]);
        Map.injectSceneService(injectables[InjectableTypes.SCENE.ordinal()]);
        Map.injectSoundService(injectables[InjectableTypes.SOUND.ordinal()]);

        ((SceneService) injectables[InjectableTypes.SCENE.ordinal()]).changeScene(SceneType.TITLE);
        gameController.start();
        gameController.initializeLoop();
        renderController.startRendering();
    }

    public static void exit() {
        for (Injectable injectable : injectables)
            injectable.stopService();

        renderController.stopRendering();
        System.exit(0);
    }
}
