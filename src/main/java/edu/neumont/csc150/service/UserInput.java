package edu.neumont.csc150.service;

import edu.neumont.csc150.model.misc.Vector3;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.robot.Robot;

import java.util.HashSet;
import java.util.Set;

public class UserInput implements Injectable {
    private volatile KeyCode keyPressed;
    private final Set<KeyCode> keyDown = new HashSet<>();
    private volatile KeyCode keyUp;

    private volatile MouseEvent leftMouse;
    private volatile MouseEvent middleMouse;
    private volatile MouseEvent rightMouse;
    private volatile Vector3 mousePosition = Vector3.zero();
    public volatile boolean lockMouse = false;
    private volatile boolean resetMouse = false;
    private Robot robot;

    private final Scene scene;

    public UserInput(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void startService() {
        startKeyboardService();
        startMouseService();
        robot = new Robot();
    }

    @Override
    public void stopService() {}

    private void startMouseService() {
        scene.setOnMousePressed((event) -> {
            switch (event.getButton()) {
                case PRIMARY -> leftMouse = event;
                case MIDDLE -> middleMouse = event;
                case SECONDARY -> rightMouse = event;
            }
        });

        scene.setOnMouseReleased((event) -> {
            switch (event.getButton()) {
                case PRIMARY -> leftMouse = event;
                case MIDDLE -> middleMouse = event;
                case SECONDARY -> rightMouse = event;
            }
        });

        scene.setOnMouseMoved((event) -> {
            int widthCenter = (int)(scene.getWidth() / 2.0);
            int heightCenter = (int)(scene.getHeight() / 2.0);

            mousePosition = new Vector3(
                    (int)(event.getX() - widthCenter),
                    (int)(event.getY() - heightCenter),
                    0
            );

            if (lockMouse && resetMouse) {
                double centerX = scene.getWindow().getX() + scene.getWindow().getWidth() / 2.0;
                double centerY = scene.getWindow().getY() + scene.getWindow().getHeight() / 2.0;
                robot.mouseMove(centerX, centerY);
                resetMouse = false;
            }
        });
    }

    public synchronized boolean mouseLocked() {
        return lockMouse;
    }

    public synchronized MouseEvent getLeftMouse() {
        return leftMouse;
    }

    public synchronized MouseEvent getMiddleMouse() {
        return middleMouse;
    }

    public synchronized MouseEvent getRightMouse() {
        return rightMouse;
    }

    public synchronized Vector3 getMouseMovement() {
        if (resetMouse) return Vector3.zero();

        Vector3 mouse = new Vector3(mousePosition);
        resetMouse = true;
        return mouse;
    }

    private synchronized void startKeyboardService() {
        scene.setOnKeyPressed((event) -> {
            keyPressed = event.getCode();
            keyDown.add(keyPressed);
        });

        scene.setOnKeyReleased((event) -> {
            keyUp = event.getCode();
            keyDown.remove(keyUp);
        });
    }

    public synchronized char getKeyPressed() {
        if (keyPressed == null)
            return KeyCode.CLEAR.getChar().charAt(0);

        char key = keyPressed.getChar().charAt(0);

        keyPressed = null;

        return key;
    }

    public synchronized Set<KeyCode> getKeyDown() {
        return new HashSet<>(keyDown);
    }

    public synchronized char getKeyUp() {
        if (keyUp == null)
            return KeyCode.CLEAR.getChar().charAt(0);

        char key = keyUp.getChar().charAt(0);

        keyUp = null;

        return key;
    }
}
