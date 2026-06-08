package de.omegazirkel.risingworld.tools.utils;

public enum RegionType {
    FOREST("Forest"),
    ARCTIC("Arctic"),
    DESERT("Desert"),
    UNKNOWN("Unknown");

    public final String name;

    RegionType(String name) {
        this.name = name;
    }
}
