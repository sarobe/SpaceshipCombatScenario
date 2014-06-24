package common;

import controller.ConditionActionController;
import controller.Controller;
import controller.mcts.ShipBiasedMCTSController;
import controller.statebased.basic.GreedyController;
import controller.statebased.basic.MCController;
import controller.statebased.basic.NullController;
import spaceship.Spaceship;

public class RunParameters {


    // master enumeration of parameters to go through
    // (a better structure would likely be more OOP, but i'm not sure how to link that to the Constants fields without reflection)
    // (dictionary structure? perhaps a total rewrite of the code is in order after this first test run)

    public static enum RunParameterEnums {
        // note lack of ship controller enum: ship controller is special, everything will be tested for ship controller
        WORLD_TYPE(3),
        FRICTION(3),
        ASTEROID_COUNT(3),
        ASTEROID_SIZE(3),
        ASTEROID_SPEED(3),
        THRUSTER_SPEED(3);

        private final int numValues;

        RunParameterEnums(int numValues) {
            this.numValues = numValues;
        }

        public int getNumValues() {
            return numValues;
        }
    }

    public static RunParameterEnums currentRunVariable;
    public static int numTrials = 1;

    public static enum ShipController {
        GREEDY_SEARCH,
        CONDITION_ACTION,
        FLAT_MC,
        MCTS
    }

    public static ShipController runShipController;

    // to a) avoid reflection and b) writing repeat copies of this code chunk every single place it's wanted
    public static Controller getAppropriateController(ShipController specifiedController, Spaceship ship, Spaceship antagonist, boolean isPredator) {
        Controller cont;
        switch (specifiedController) {
            case GREEDY_SEARCH:
                cont = new GreedyController(ship, antagonist, isPredator);
                break;
            case CONDITION_ACTION:
                cont = new ConditionActionController(ship, antagonist, isPredator);
                break;
            case FLAT_MC:
                cont = new MCController(ship, antagonist, isPredator);
                break;
            case MCTS:
                cont = new ShipBiasedMCTSController(ship, antagonist, isPredator);
                break;
            default:
                cont = new NullController(ship, antagonist, isPredator);
        }
        return cont;
    }

    // WorldType is defined in Constants
    public static Constants.WorldType defaultWorldType = Constants.WorldType.CIRCULAR;

    // associated global: Constants.friction
    public static double[] runFrictionConstants = {
            1.00,
            0.99,
            0.8
    };
    public static double defaultFriction = 0.99;

    // associated global: Constants.numAsteroids
    public static int[] runAsteroidCounts = {
            0,
            4,
            8
    };
    public static int defaultAsteroidCount = 4;

    // associated globals: Constants.minAsteroidRadius, Constants.maxAsteroidRadius
    public static int[] runAsteroidSizeRanges = {
            1, 5,
            5, 15,
            15, 25
    };
    public static int[] defaultAsteroidSizeRange = {5, 15};

    // associated global: Constants.maxVelocityRange
    public static double[] runAsteroidMaxVelocityRanges = {
            0,
            20,
            40
    };
    public static double defaultAsteroidMaxVelocityRange = 20;

    // associated global: Constants.thrusterThrust
    public static double[] runThrusts = {
            500,
            1000,
            2000
    };
    public static double defaultThrust = 1000;


    // and now a method to tie it all together, save the ship controller stuff which should be handled separately
    public static void setParameter(RunParameterEnums runParameter, int index) {
        // first set all parameters to defaults
        Constants.worldType = defaultWorldType;
        Constants.friction = defaultFriction;
        Constants.numAsteroids = defaultAsteroidCount;
        Constants.minAsteroidRadius = defaultAsteroidSizeRange[0];
        Constants.maxAsteroidRadius = defaultAsteroidSizeRange[1];
        Constants.maxVelocityRange = defaultAsteroidMaxVelocityRange;
        Constants.thrusterThrust = defaultThrust;

        // then set the specific parameter requested
        // if the index is invalid, don't change the parameter
        switch (runParameter) {
            case WORLD_TYPE:
                if ( index >= 0 && index < Constants.WorldType.values().length ) {
                    Constants.worldType = Constants.WorldType.values()[index];
                }
                break;
            case FRICTION:
                if ( index >= 0 && index < runFrictionConstants.length ) {
                    Constants.friction = runFrictionConstants[index];
                }
                break;
            case ASTEROID_COUNT:
                if ( index > 0 && index < runAsteroidCounts.length ) {
                    Constants.numAsteroids = runAsteroidCounts[index];
                }
                break;
            case ASTEROID_SIZE:
                if ( index > 0 && index < runAsteroidSizeRanges.length ) {
                    Constants.minAsteroidRadius = runAsteroidSizeRanges[index * 2];
                    Constants.maxAsteroidRadius = runAsteroidSizeRanges[index * 2 + 1];
                }
                break;
            case ASTEROID_SPEED:
                if ( index > 0 && index < runAsteroidMaxVelocityRanges.length ) {
                    Constants.maxVelocityRange = runAsteroidMaxVelocityRanges[index];
                }
                break;
            case THRUSTER_SPEED:
                if ( index > 0 && index < runThrusts.length ) {
                    Constants.thrusterThrust = runThrusts[index];
                }
                break;
        }
    }

    public static String outputValues() {
        String output = "";
        output += "World Type: " + Constants.worldType;
        output += "\nFriction: " + Constants.friction;
        output += "\nNum. of Asteroids: " + Constants.numAsteroids;
        output += "\nAsteroid Radius: " + Constants.minAsteroidRadius + " - " + Constants.maxAsteroidRadius;
        output += "\nMax. Asteroid Velocity: " + Constants.maxVelocityRange;
        output += "\nThruster Thrust: " + Constants.thrusterThrust;
        return output;
    }


}
