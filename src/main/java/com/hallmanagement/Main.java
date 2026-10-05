package com.hallmanagement;

import com.hallmanagement.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        SceneManager.switchTo(primaryStage, SceneManager.LOGIN_FXML, SceneManager.APP_TITLE);

        primaryStage.setMinWidth(SceneManager.MIN_WIDTH);
        primaryStage.setMinHeight(SceneManager.MIN_HEIGHT);

        primaryStage.setOnCloseRequest(event -> {
            javafx.application.Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
