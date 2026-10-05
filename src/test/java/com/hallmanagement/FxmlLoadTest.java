package com.hallmanagement;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class FxmlLoadTest {
    public static void main(String[] args) {
        try {

            Platform.startup(() -> {});

            System.out.println("Testing loading of /com/hallmanagement/view/HallInformation.fxml...");
            FXMLLoader loader = new FXMLLoader(FxmlLoadTest.class.getResource("/com/hallmanagement/view/HallInformation.fxml"));
            Parent root = loader.load();
            System.out.println("SUCCESSFULLY LOADED HallInformation.fxml! Root = " + root);
            System.exit(0);
        } catch (Throwable t) {
            System.err.println("FAILED TO LOAD HallInformation.fxml: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
