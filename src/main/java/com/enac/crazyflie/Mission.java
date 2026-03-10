package com.enac.crazyflie;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Mission {

    @JsonProperty("waypoints")
    private List<MainController.Waypoint> waypoints;

    @JsonProperty("velocity")
    private double velocity;

    @JsonProperty("canvasWidth")
    private double canvasWidth;

    @JsonProperty("canvasHeight")
    private double canvasHeight;

    public Mission() {}

    public Mission(List<MainController.Waypoint> waypoints, double velocity,
                   double canvasWidth, double canvasHeight) {
        this.waypoints    = waypoints;
        this.velocity     = velocity;
        this.canvasWidth  = canvasWidth;
        this.canvasHeight = canvasHeight;
    }

    public void saveToFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filePath), this);
    }

    public static Mission loadFromFile(String filePath) throws IOException {
        return new ObjectMapper().readValue(new File(filePath), Mission.class);
    }

    public String generatePythonScript() {
        if (waypoints == null || waypoints.isEmpty()) {
            throw new IllegalStateException("No waypoints defined in mission.");
        }

        final double PX_PER_METRE = 100.0;

        StringBuilder sb = new StringBuilder();

        sb.append("\"\"\"\n");
        sb.append("  Waypoints : ").append(waypoints.size()).append("\n");
        sb.append("  Velocity  : ").append(velocity).append(" m/s\n");
        sb.append("\"\"\"\n\n");

        sb.append("import time\n");
        sb.append("import cflib.crtp\n");
        sb.append("from cflib.crazyflie import Crazyflie\n");
        sb.append("from cflib.crazyflie.syncCrazyflie import SyncCrazyflie\n");
        sb.append("from cflib.positioning.position_hl_commander import PositionHlCommander\n\n");

        sb.append("URI = 'radio://0/80/2M/E7E7E7E7E7'\n\n");

        sb.append("WAYPOINTS = [\n");
        for (MainController.Waypoint wp : waypoints) {
            double worldX = wp.getX() / PX_PER_METRE;
            double worldY = wp.getY() / PX_PER_METRE;
            sb.append(String.format("    (%.3f, %.3f, %.3f),\n",
                    worldX, worldY, wp.getAltitude()));
        }
        sb.append("]\n\n");

        sb.append("def run_mission(scf):\n");
        sb.append("    with PositionHlCommander(\n");
        sb.append("            scf,\n");
        sb.append("            default_velocity=").append(velocity).append(",\n");
        sb.append("            default_height=").append(waypoints.get(0).getAltitude()).append("\n");
        sb.append("    ) as pc:\n");
        sb.append("        time.sleep(1.0)  # stabilise after takeoff\n\n");
        sb.append("        for i, (x, y, z) in enumerate(WAYPOINTS):\n");
        sb.append("            print(f'Going to waypoint {i + 1}/{len(WAYPOINTS)}: x={x}, y={y}, z={z}')\n");
        sb.append("            pc.go_to(x, y, z)\n");
        sb.append("            time.sleep(0.5)\n\n");
        sb.append("        print('Mission complete. Landing...')\n\n");

        sb.append("if __name__ == '__main__':\n");
        sb.append("    cflib.crtp.init_drivers()\n");
        sb.append("    with SyncCrazyflie(URI, cf=Crazyflie(rw_cache='./cache')) as scf:\n");
        sb.append("        run_mission(scf)\n");

        return sb.toString();
    }

    public List<MainController.Waypoint> getWaypoints()              { return waypoints; }
    public void setWaypoints(List<MainController.Waypoint> waypoints){ this.waypoints = waypoints; }

    public double getVelocity()              { return velocity; }
    public void   setVelocity(double v)      { this.velocity = v; }

    public double getCanvasWidth()           { return canvasWidth; }
    public void   setCanvasWidth(double w)   { this.canvasWidth = w; }

    public double getCanvasHeight()          { return canvasHeight; }
    public void   setCanvasHeight(double h)  { this.canvasHeight = h; }
}