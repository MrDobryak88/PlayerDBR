// Main.java
package com.example.musicplayer;

import javafx.application.Application;
import javafx.stage.Stage;
import com.example.musicplayer.view.PlayerView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        PlayerView playerView = new PlayerView();
        playerView.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}