package controller.mcts;

import common.Constants;
import common.math.Vector2d;
import problem.Pickup;
import problem.PickupType;
import spaceship.Spaceship;

import java.util.Map;

/**
 * Created by Samuel Roberts, 2013
 */
public class InfluenceMap {

    public static final int CELL_SIZE = 32;
    static final double MAX_DIST = 1280;

    static double influenceMap[][];
    static int mapWidth;
    static int mapHeight;

    static double lowestValue;
    static double highestValue;

    // influence map for pickup collection problem
    public static void createInfluenceMap(Map<Pickup, Boolean> pickupStates) {
        mapWidth = Constants.screenWidth / CELL_SIZE;
        mapHeight = Constants.screenHeight / CELL_SIZE;
        influenceMap = new double[mapWidth][mapHeight];

        lowestValue = Double.MAX_VALUE;
        highestValue = -Double.MAX_VALUE;

        Vector2d cellCenter = new Vector2d();
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                // get sum of all proximities to pickups here
                for (Pickup p : pickupStates.keySet()) {
                    if (!pickupStates.get(p)) {
                        // existing pickup
                        cellCenter.set(CELL_SIZE * (x + 0.5), CELL_SIZE * (y + 0.5));
                        double dist = cellCenter.dist(p.pos);
                        //// use exponential dropoff instead of linear dropoff
                        double value = 5 + Math.log1p(dist) * -1;
                        //double value = (MAX_DIST - dist)/MAX_DIST;
                        if (p.type == PickupType.MINE) value *= 0;

                        influenceMap[x][y] += value * 50;
                    }
                }
            }
        }

        // go through map and establish ranges
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                double value = influenceMap[x][y];
                if (value > highestValue) highestValue = value;
                if (value < lowestValue) lowestValue = value;
            }
        }

    }

    // influence map for predator prey problem
    public static void createInfluenceMap(Spaceship predator, Spaceship prey) {
        mapWidth = Constants.screenWidth / CELL_SIZE;
        mapHeight = Constants.screenHeight / CELL_SIZE;
        influenceMap = new double[mapWidth][mapHeight];
        lowestValue = Double.MAX_VALUE;
        highestValue = -Double.MAX_VALUE;

        Vector2d cellCenter = new Vector2d();
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                cellCenter.set(CELL_SIZE * (x + 0.5), CELL_SIZE * (y + 0.5));
                double dist = cellCenter.dist(prey.pos);
                // use exponential dropoff instead of linear dropoff
                //double value = 5 + Math.log1p(dist) * -1;
                double value = (MAX_DIST - dist)/MAX_DIST;
                influenceMap[x][y] += value;
            }
        }

        // go through map and establish ranges
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                double value = influenceMap[x][y];
                if (value > highestValue) highestValue = value;
                if (value < lowestValue) lowestValue = value;
            }
        }

    }

    public static double[][] getMap() {
        return influenceMap;
    }

    public static int getWidth() {
        return mapWidth;
    }

    public static int getHeight() {
        return mapHeight;
    }

    public static double getValue(double x, double y) {
        int col = (int) (x / CELL_SIZE) % mapWidth;
        int row = (int) (y / CELL_SIZE) % mapHeight;
        return influenceMap[col][row];
    }

    public static double getValueAtCell(int x, int y) {
        return influenceMap[x][y];
    }

    public static double getHighest() {
        return highestValue;
    }

    public static double getLowest() {
        return lowestValue;
    }

    public static double getRange() {
        return highestValue - lowestValue;
    }

    public static double getNormalisedValue(double value) {
        return (value - lowestValue) / getRange();
    }
}
