package com.enac.crazyflie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {

    // ─────────────────────────────────────────────
    //  FXML Bindings
    // ─────────────────────────────────────────────

    @FXML private Canvas    trajectoryCanvas;
    @FXML private TextField altitudeField;
    @FXML private TextField velocityField;
    @FXML private Label     statusLabel;

    // ─────────────────────────────────────────────
    //  State
    // ─────────────────────────────────────────────

    private final List<Waypoint> waypoints = new ArrayList<>();
    private boolean drawing = false;

    // ─────────────────────────────────────────────
    //  Init
    // ─────────────────────────────────────────────

    @FXML
    private void initialize() {
        drawTrajectory();
        setStatus("READY", StatusType.OK);
    }

    // ─────────────────────────────────────────────
    //  Canvas
    // ─────────────────────────────────────────────

    @FXML
    private void canvasClicked(MouseEvent event) {
        if (!drawing) return;
        double x   = event.getX();
        double y   = event.getY();
        double alt = parseDouble(altitudeField.getText(), 1.0);
        waypoints.add(new Waypoint(x, y, alt));
        drawTrajectory();
        setStatus(String.format("WP%02d  ->  X:%.0f  Y:%.0f  ALT:%.1fm",
                waypoints.size(), x, y, alt), StatusType.OK);
    }

    // ─────────────────────────────────────────────
    //  Drawing controls
    // ─────────────────────────────────────────────

    @FXML private void startDrawing() {
        drawing = true;
        setStatus("DRAWING MODE  -  CLICK ON MAP TO ADD WAYPOINTS", StatusType.OK);
    }

    @FXML private void stopDrawing() {
        drawing = false;
        setStatus("DRAWING STOPPED  -  " + waypoints.size() + " WAYPOINTS RECORDED", StatusType.IDLE);
    }

    // ─────────────────────────────────────────────
    //  Add Waypoint — manual dialog
    // ─────────────────────────────────────────────

    @FXML
    private void addWaypoint() {
        Dialog<Waypoint> dialog = new Dialog<>();
        dialog.setTitle("Add Waypoint");
        dialog.setHeaderText("Enter waypoint coordinates");

        ButtonType addBtn = new ButtonType("ADD", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField xField   = styledField("0");
        TextField yField   = styledField("0");
        TextField altField = styledField(altitudeField.getText());

        grid.add(new Label("X (px):"),       0, 0); grid.add(xField,   1, 0);
        grid.add(new Label("Y (px):"),       0, 1); grid.add(yField,   1, 1);
        grid.add(new Label("Altitude (m):"), 0, 2); grid.add(altField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> btn == addBtn
                ? new Waypoint(parseDouble(xField.getText(), 0),
                               parseDouble(yField.getText(), 0),
                               parseDouble(altField.getText(), 1.0))
                : null);

        dialog.showAndWait().ifPresent(wp -> {
            waypoints.add(wp);
            drawTrajectory();
            setStatus(String.format("WP%02d ADDED  ->  X:%.0f  Y:%.0f  ALT:%.1fm",
                    waypoints.size(), wp.getX(), wp.getY(), wp.getAltitude()), StatusType.OK);
        });
    }

    // ─────────────────────────────────────────────
    //  Clear
    // ─────────────────────────────────────────────

    @FXML
    private void clearTrajectory() {
        if (waypoints.isEmpty()) { setStatus("NO WAYPOINTS TO CLEAR", StatusType.IDLE); return; }
        confirmDialog("Clear Trajectory", "Delete all " + waypoints.size() + " waypoints?", () -> {
            waypoints.clear();
            drawTrajectory();
            setStatus("TRAJECTORY CLEARED", StatusType.IDLE);
        });
    }

    // ─────────────────────────────────────────────
    //  File — New
    // ─────────────────────────────────────────────

    @FXML
    private void newMission() {
        if (waypoints.isEmpty()) { resetMission(); return; }
        confirmDialog("New Mission", "Unsaved waypoints will be lost. Continue?", this::resetMission);
    }

    private void resetMission() {
        waypoints.clear();
        drawTrajectory();
        setStatus("NEW MISSION STARTED", StatusType.OK);
    }

    // ─────────────────────────────────────────────
    //  File — Save  (delegates to Mission)
    // ─────────────────────────────────────────────

    @FXML
    private void saveMission() {
        if (waypoints.isEmpty()) {
            setStatus("NOTHING TO SAVE  -  ADD WAYPOINTS FIRST", StatusType.IDLE);
            return;
        }

        File file = fileChooser("Save Mission", "mission.json", true);
        if (file == null) return;

        try {
            buildMission().saveToFile(file.getAbsolutePath());
            setStatus("MISSION SAVED  ->  " + file.getName(), StatusType.OK);
        } catch (IOException e) {
            showError("Save Failed", e.getMessage());
            setStatus("SAVE ERROR", StatusType.ERROR);
        }
    }

    // ─────────────────────────────────────────────
    //  File — Load  (delegates to Mission)
    // ─────────────────────────────────────────────

    @FXML
    private void loadMission() {
        File file = fileChooser("Load Mission", null, false);
        if (file == null) return;

        try {
            Mission mission = Mission.loadFromFile(file.getAbsolutePath());

            waypoints.clear();
            if (mission.getWaypoints() != null)
                waypoints.addAll(mission.getWaypoints());

            velocityField.setText(String.valueOf(mission.getVelocity()));

            drawTrajectory();
            setStatus("MISSION LOADED  ->  " + file.getName()
                    + "  [" + waypoints.size() + " WAYPOINTS]", StatusType.OK);
        } catch (IOException e) {
            showError("Load Failed", e.getMessage());
            setStatus("LOAD ERROR", StatusType.ERROR);
        }
    }

    // ─────────────────────────────────────────────
    //  Generate Python Script  (delegates to Mission)
    // ─────────────────────────────────────────────

    @FXML
    private void generateScript() {
        if (waypoints.isEmpty()) {
            setStatus("NO WAYPOINTS  -  ADD WAYPOINTS BEFORE GENERATING", StatusType.IDLE);
            return;
        }

        File file = fileChooser("Save Python Script", "mission.py", true);
        if (file == null) return;

        try {
            String script = buildMission().generatePythonScript();
            Files.writeString(file.toPath(), script);
            setStatus("SCRIPT GENERATED  ->  " + file.getName(), StatusType.OK);
        } catch (IOException | IllegalStateException e) {
            showError("Script Generation Failed", e.getMessage());
            setStatus("SCRIPT ERROR", StatusType.ERROR);
        }
    }

    // ─────────────────────────────────────────────
    //  Execute Mission  (async simulation)
    // ─────────────────────────────────────────────

    @FXML
    private void executeMission() {
        if (waypoints.isEmpty()) { setStatus("NO WAYPOINTS DEFINED", StatusType.IDLE); return; }

        confirmDialog("Execute Mission",
                "Send " + waypoints.size() + " waypoints to drone and start mission?",
                () -> {
                    Task<Void> task = buildExecutionTask();
                    task.setOnFailed(e -> Platform.runLater(() -> {
                        setStatus("MISSION FAILED", StatusType.ERROR);
                        showError("Execution Error", task.getException().getMessage());
                    }));
                    Thread t = new Thread(task);
                    t.setDaemon(true);
                    t.start();
                });
    }

    private Task<Void> buildExecutionTask() {
        List<Waypoint> snapshot = List.copyOf(waypoints);
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> setStatus("CONNECTING TO DRONE...", StatusType.IDLE));
                Thread.sleep(800);
                Platform.runLater(() -> setStatus("LINK ESTABLISHED  -  UPLOADING MISSION...", StatusType.OK));
                Thread.sleep(600);

                for (int i = 0; i < snapshot.size(); i++) {
                    final int    idx = i + 1;
                    final Waypoint wp = snapshot.get(i);
                    Platform.runLater(() ->
                            setStatus(String.format("EXECUTING  WP%02d / %02d  ->  ALT: %.1fm",
                                    idx, snapshot.size(), wp.getAltitude()), StatusType.OK));

                    long delay = (i == 0) ? 1000
                            : (long)(distanceTo(snapshot.get(i - 1), wp) * 30);
                    Thread.sleep(Math.min(Math.max(delay, 500), 2000));

                    final int fi = i;
                    Platform.runLater(() -> animateWaypointReached(fi));
                }

                Platform.runLater(() -> setStatus("MISSION COMPLETE  -  LANDING...", StatusType.OK));
                Thread.sleep(1200);
                Platform.runLater(() -> setStatus("LANDED  -  MISSION SUCCESS", StatusType.OK));
                return null;
            }
        };
    }

    // ─────────────────────────────────────────────
    //  Exit
    // ─────────────────────────────────────────────

    @FXML private void exit() { Platform.exit(); }

    // ─────────────────────────────────────────────
    //  Canvas rendering
    // ─────────────────────────────────────────────

    private void drawTrajectory() {
        GraphicsContext gc = trajectoryCanvas.getGraphicsContext2D();
        double w = trajectoryCanvas.getWidth();
        double h = trajectoryCanvas.getHeight();

        gc.setFill(Color.web("#080c14"));
        gc.fillRect(0, 0, w, h);

        // Grid
        gc.setStroke(Color.web("#1e3a5f", 0.5));
        gc.setLineWidth(0.5);
        for (double x = 0; x < w; x += 50) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y < h; y += 50) gc.strokeLine(0, y, w, y);

        // Origin crosshair
        gc.setStroke(Color.web("#0ea5e9", 0.2));
        gc.setLineWidth(1);
        gc.strokeLine(w / 2, 0, w / 2, h);
        gc.strokeLine(0, h / 2, w, h / 2);

        if (waypoints.isEmpty()) return;

        // Trajectory lines
        gc.setStroke(Color.web("#0ea5e9"));
        gc.setLineWidth(2);
        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint p = waypoints.get(i - 1), c = waypoints.get(i);
            gc.strokeLine(p.getX(), p.getY(), c.getX(), c.getY());
        }

        // Markers
        gc.setFont(Font.font("Consolas", 10));
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);
            double cx = wp.getX(), cy = wp.getY();

            gc.setStroke(Color.web("#0ea5e9", 0.25));
            gc.setLineWidth(6);
            gc.strokeOval(cx - 10, cy - 10, 20, 20);

            if      (i == 0)                    gc.setFill(Color.web("#22d3a0"));
            else if (i == waypoints.size() - 1) gc.setFill(Color.web("#f43f5e"));
            else                                gc.setFill(Color.web("#0ea5e9"));
            gc.fillOval(cx - 5, cy - 5, 10, 10);

            gc.setFill(Color.web("#e2eaf4"));
            gc.fillText(String.format("WP%02d", i + 1), cx + 8, cy - 6);
        }
    }

    private void animateWaypointReached(int index) {
        GraphicsContext gc = trajectoryCanvas.getGraphicsContext2D();
        Waypoint wp = waypoints.get(index);
        gc.setStroke(Color.web("#22d3a0", 0.8));
        gc.setLineWidth(3);
        gc.strokeOval(wp.getX() - 14, wp.getY() - 14, 28, 28);
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

    /** Builds a Mission snapshot from current UI state. */
    private Mission buildMission() {
        return new Mission(
                new ArrayList<>(waypoints),
                parseDouble(velocityField.getText(), 0.5),
                trajectoryCanvas.getWidth(),
                trajectoryCanvas.getHeight()
        );
    }

    private File fileChooser(String title, String defaultName, boolean saveMode) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        boolean isPython = defaultName != null && defaultName.endsWith(".py");
        fc.getExtensionFilters().add(isPython
                ? new FileChooser.ExtensionFilter("Python Files", "*.py")
                : new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        if (defaultName != null) fc.setInitialFileName(defaultName);
        return saveMode
                ? fc.showSaveDialog(trajectoryCanvas.getScene().getWindow())
                : fc.showOpenDialog(trajectoryCanvas.getScene().getWindow());
    }

    private void confirmDialog(String title, String message, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> { if (btn == ButtonType.YES) onConfirm.run(); });
    }

    private enum StatusType { OK, IDLE, ERROR }

    private void setStatus(String text, StatusType type) {
        statusLabel.setText(text);
        statusLabel.setStyle("-fx-text-fill: " + switch (type) {
            case OK    -> "#22d3a0";
            case IDLE  -> "#f59e0b";
            case ERROR -> "#f43f5e";
        } + ";");
        FadeTransition ft = new FadeTransition(Duration.millis(200), statusLabel);
        ft.setFromValue(0.3);
        ft.setToValue(1.0);
        ft.play();
    }

    private static double parseDouble(String text, double fallback) {
        try { return Double.parseDouble(text.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static double distanceTo(Waypoint a, Waypoint b) {
        double dx = b.getX() - a.getX(), dy = b.getY() - a.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static TextField styledField(String val) {
        TextField tf = new TextField(val);
        tf.setPrefWidth(120);
        return tf;
    }

    private static void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ─────────────────────────────────────────────
    //  Waypoint  (Jackson-serialisable)
    // ─────────────────────────────────────────────

    public static class Waypoint {

        @JsonProperty("x")        private final double x;
        @JsonProperty("y")        private final double y;
        @JsonProperty("altitude") private final double altitude;

        /** No-arg constructor required by Jackson. */
        @JsonCreator
        public Waypoint(
                @JsonProperty("x")        double x,
                @JsonProperty("y")        double y,
                @JsonProperty("altitude") double altitude) {
            this.x = x;
            this.y = y;
            this.altitude = altitude;
        }

        public double getX()        { return x; }
        public double getY()        { return y; }
        public double getAltitude() { return altitude; }
    }
}