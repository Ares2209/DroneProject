package com.enac.crazyflie;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Mission {

    @JsonProperty("waypoints")
    private List<MainController.Waypoint> waypoints;

    @JsonProperty("velocity")
    private double velocity;

    public Mission() {}

    public Mission(List<MainController.Waypoint> waypoints, double velocity) {
        this.waypoints = waypoints;
        this.velocity = velocity;
    }

    public List<MainController.Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<MainController.Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void saveToFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(filePath), this);
    }

    public static Mission loadFromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), Mission.class);
    }

    // Method to generate Python script
    public String generatePythonScript() {
        StringBuilder script = new StringBuilder();
        script.append("# Generated Crazyflie mission script\n");
        script.append("import cflib\n");
        script.append("from cflib.crazyflie import Crazyflie\n");
        script.append("from cflib.crazyflie.syncCrazyflie import SyncCrazyflie\n");
        script.append("from cflib.positioning.position_hl_commander import PositionHlCommander\n");
        script.append("\n");
        script.append("# Initialize Crazyflie\n");
        script.append("cf = Crazyflie(rw_cache='./cache')\n");
        script.append("scf = SyncCrazyflie('radio://0/80/2M/E7E7E7E7E7', cf=cf)\n");
        script.append("scf.open_link()\n");
        script.append("pc = PositionHlCommander(scf, default_velocity=").append(velocity).append(", default_height=0.5)\n");
        script.append("pc.take_off(0.5)\n");
        script.append("\n");

        for (int i = 1; i < waypoints.size(); i++) {
            MainController.Waypoint prev = waypoints.get(i - 1);
            MainController.Waypoint curr = waypoints.get(i);
            script.append("pc.go_to(").append(curr.getX() / 100).append(", ").append(curr.getY() / 100).append(", ").append(curr.getAltitude()).append(")\n");
        }

        script.append("\n");
        script.append("pc.land()\n");
        script.append("scf.close_link()\n");

        return script.toString();
    }
}