package com.enac.crazyflie;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Drone {
    
    @JsonProperty("maxvelocity")
    private double maxvelocity;

    @JsonProperty("maxaltitude")
    private double maxaltitude;

    @JsonProperty("maxdistance")
    private double maxdistance;

    @JsonProperty("name")
    private String name;

    public Drone() {
        
    }

}
