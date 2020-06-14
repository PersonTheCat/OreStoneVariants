package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.HashGenerator;

public class RandomChunkSelector {

    /** The hasher to be used for selecting chunks. */
    private final HashGenerator noise;

    /** Reflects the probability of selection for any given chunk. */
    private static final double SELECTION_THRESHOLD = 91.0; // Highest possible value.
    /** Highest possible probability in selected chunks. */
    private static final double MAX_PROBABILITY = Cfg.WorldCat.largeClusterMaxProbability;
    /** Lowest possible probability for any given chunk. */
    private static final double DEFAULT_PROBABILITY = Cfg.WorldCat.largeClusterDefaultProbability;
    /** The radius of chunks to search outward. */
    private static final int DISTANCE = 2;

    public RandomChunkSelector(long worldSeed) {
        this.noise = new HashGenerator(worldSeed);
    }

    /**
     * Obtain a random value from the three inputs using HashGenerator.
     * The threshold reflects the probability of selection.
     */
    public boolean testCoordinates(int ID, int x, int y) {
        return noise.getHash(ID, x, y) > SELECTION_THRESHOLD;
    }

    public double getProbability(int ID, int x, int y) {
        if (testCoordinates(ID, x, y)) {
            return MAX_PROBABILITY;
        }
        for (int i = 1; i <= DISTANCE; i++) {
            if (testDistance(ID, x, y, i)) {
                // (80) -> 40 -> 20 -> etc.
                return (int) MAX_PROBABILITY >> i;
            }
        }
        return DEFAULT_PROBABILITY;
    }

    /** Tests all coordinates +- radius. */
    private boolean testDistance(int ID, int x, int y, int radius) {
        final int diameter = (radius * 2) + 1;
        final int innerLength = diameter - 2;
        final int shift = -(radius - 1);

        // Test the corners;
        if (testCoordinates(ID, x + radius, y + radius)
            || testCoordinates(ID, x - radius, y - radius)
            || testCoordinates(ID, x + radius, y - radius)
            || testCoordinates(ID, x - radius, y + radius)) {
            return true;
        }
        // Test the sides.
        for (int i = shift; i < innerLength + shift; i++) {
            if (testCoordinates(ID, x + radius, y + i)
                || testCoordinates(ID, x + i, y + radius)
                || testCoordinates(ID, x - radius, y + i)
                || testCoordinates(ID, x + i, y - radius)) {
                return true;
            }
        }
        return false;
    }
}