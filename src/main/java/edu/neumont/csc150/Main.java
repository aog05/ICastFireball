package edu.neumont.csc150;

import edu.neumont.csc150.controller.InitController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        InitController.init(stage);
    }

    public static void main(String[] args) {
        System.setProperty("jline.terminal", "dumb");
        launch(args);
    }
}
