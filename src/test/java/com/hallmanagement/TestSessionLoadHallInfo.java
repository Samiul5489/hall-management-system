package com.hallmanagement;

import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.model.User;
import com.hallmanagement.util.SceneManager;
import com.hallmanagement.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class TestSessionLoadHallInfo {
    public static void main(String[] args) {
        try {
            Platform.startup(() -> {});

            User studentUser = new User(1, "2003001", "Student", "01700000000", "STUDENT", "ACTIVE");
            SessionManager.getInstance().login(studentUser);
            System.out.println("Set student session: " + studentUser.getUserId());

            FXMLLoader loader1 = new FXMLLoader(TestSessionLoadHallInfo.class.getResource(SceneManager.HALL_INFORMATION_FXML));
            Parent r1 = loader1.load();
            System.out.println("Loaded HallInformation.fxml with Student session successfully! Root=" + r1);

            User provostUser = new User(2, "provost1", "Provost", "01711111111", "PROVOST", "ACTIVE");
            SessionManager.getInstance().login(provostUser);
            System.out.println("Set provost session: " + provostUser.getUserId());

            FXMLLoader loader2 = new FXMLLoader(TestSessionLoadHallInfo.class.getResource(SceneManager.HALL_INFORMATION_FXML));
            Parent r2 = loader2.load();
            System.out.println("Loaded HallInformation.fxml with Provost session successfully! Root=" + r2);

            System.exit(0);
        } catch (Throwable t) {
            System.err.println("CRASH DURING SESSION LOAD: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
