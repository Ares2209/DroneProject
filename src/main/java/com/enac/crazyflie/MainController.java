package com.enac.crazyflie;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private Canvas trajectoryCanvas;

    @FXML
    private TextField altitudeField;

    @FXML
    private TextField velocityField;

    @FXML
    private Label statusLabel;

    private List<Waypoint> waypoints = new ArrayList<>();
    private boolean drawing = false;

    @FXML
    private void initialize() {
        GraphicsContext gc = trajectoryCanvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, trajectoryCanvas.getWidth(), trajectoryCanvas.getHeight());
    }

    @FXML
    private void canvasClicked(MouseEvent event) {
        if (drawing) {
            double x = event.getX();
            double y = event.getY();
            double altitude = Double.parseDouble(altitudeField.getText());
            waypoints.add(new Waypoint(x, y, altitude));
            drawTrajectory();
            statusLabel.setText("Waypoint added at (" + x + ", " + y + ")");
        }
    }

    @FXML
    private void startDrawing() {
        drawing = true;
        statusLabel.setText("Drawing mode enabled");
    }

    @FXML
    private void stopDrawing() {
        drawing = false;
        statusLabel.setText("Drawing mode disabled");
    }

    @FXML
    private void addWaypoint() {
        // Logic to add waypoint programmatically
        statusLabel.setText("Add waypoint manually");
    }

    @FXML
    private void clearTrajectory() {
        waypoints.clear();
        drawTrajectory();
        statusLabel.setText("Trajectory cleared");
    }

    @FXML
    private void newMission() {
        waypoints.clear();
        drawTrajectory();
        statusLabel.setText("New mission started");
    }

    @FXML
    private void loadMission() {
        // Load from JSON
        statusLabel.setText("Load mission - not implemented yet");
    }

    @FXML
    private void saveMission() {
        // Save to JSON
        statusLabel.setText("Save mission - not implemented yet");
    }

    @FXML
    private void generateScript() {
        // Generate Python script
        statusLabel.setText("Generate script - not implemented yet");
    }

    @FXML
    private void executeMission() {
        // Execute mission
        statusLabel.setText("Execute mission - not implemented yet");
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    private void drawTrajectory() {
        GraphicsContext gc = trajectoryCanvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, trajectoryCanvas.getWidth(), trajectoryCanvas.getHeight());
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint prev = waypoints.get(i - 1);
            Waypoint curr = waypoints.get(i);
            gc.strokeLine(prev.getX(), prev.getY(), curr.getX(), curr.getY());
        }
        gc.setFill(Color.RED);
        for (Waypoint wp : waypoints) {
            gc.fillOval(wp.getX() - 5, wp.getY() - 5, 10, 10);
        }
    }

    // Inner class for Waypoint
    public static class Waypoint {
        private double x, y, altitude;

        public Waypoint(double x, double y, double altitude) {
            this.x = x;
            this.y = y;
            this.altitude = altitude;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getAltitude() { return altitude; }
    }
}