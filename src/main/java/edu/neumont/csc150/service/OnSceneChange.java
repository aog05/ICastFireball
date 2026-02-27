package edu.neumont.csc150.service;

import edu.neumont.csc150.model.enums.SceneType;

public interface OnSceneChange {
    void sceneChangeEvent(SceneType scene);
}
