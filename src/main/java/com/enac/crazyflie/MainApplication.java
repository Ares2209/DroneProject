package com.enac.crazyflie;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.*;
import javafx.stage.Screen;
import javafx.util.Duration;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.application.Platform;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);

        scene.getStylesheets().add(
            MainApplication.class.getResource("styles.css").toExternalForm()
        );

        stage.getIcons().add(
        new Image(MainApplication.class.getResourceAsStream("Logo.png"))
        );

        stage.setTitle("Crazyflie Mission Planner");
        stage.setMinWidth(800);
        stage.setMinHeight(500);

        stage.setOnCloseRequest(event -> {
            event.consume();                         
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Quitter ? Les waypoints non sauvegardés seront perdus.",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Quitter");
            confirm.setHeaderText(null);
            confirm.showAndWait()
                .filter(btn -> btn == ButtonType.YES)
                .ifPresent(btn -> Platform.exit());
        });


        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
                MainController ctrl = fxmlLoader.getController();
                ctrl.saveMission();
                event.consume();
            }

            if (new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN).match(event)) {
                MainController ctrl = fxmlLoader.getController();
                ctrl.newMission();
                event.consume();
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                MainController ctrl = fxmlLoader.getController();
                ctrl.stopDrawing();
                event.consume();
            }
        });

        scene.setCursor(Cursor.CROSSHAIR);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}