package com.enac.crazyflie;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Drone {
    
    @JsonProperty("maxVelocity")
    private double maxVelocity;

    @JsonProperty("maxAltitude")
    private double maxAltitude;

    @JsonProperty("maxDistance")
    private double maxDistance;

    @JsonProperty("name")
    private String name;

    @JsonProperty("model")
    private String model;

    public Drone(String name, String model, double maxAltitude, double maxVelocity, double maxDistance) {
        this.name = name;
        this.model = model;
        this.maxAltitude = maxAltitude;
        this.maxVelocity = maxVelocity;
        this.maxDistance = maxDistance;
    }

    public double getMaxVelocity() {
        return maxVelocity;
    }
    
    public double getMaxAltitude() {
        return maxAltitude;
    }

    public double getMaxDistance() {
        return maxDistance;
    }
    
    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

}
