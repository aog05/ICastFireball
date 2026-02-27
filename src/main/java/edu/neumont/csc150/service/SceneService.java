package edu.neumont.csc150.service;

import edu.neumont.csc150.model.enums.SceneType;

import java.util.HashSet;
import java.util.Set;

public class SceneService implements Injectable {
    private Set<OnSceneChange> triggerUpdatesOn;
    private SceneType scene;
    private SceneType previousScene;

    public void startService() {
        triggerUpdatesOn = new HashSet<>();
    }

    public void stopService() {
        triggerUpdatesOn = null;
    }

    public void changeScene(SceneType loadScene) {
        previousScene = scene;
        scene = loadScene;
        for (OnSceneChange trigger : triggerUpdatesOn)
            trigger.sceneChangeEvent(scene);
    }

    public SceneType getScene() {
        return scene;
    }

    public SceneType getPreviousScene() {
        return previousScene;
    }

    public void addOnSceneChange(OnSceneChange trigger) {
        triggerUpdatesOn.add(trigger);
    }
}
