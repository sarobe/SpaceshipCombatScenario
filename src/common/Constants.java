package common;

import common.math.Vector2d;
import controller.mcts.SimpleAction;

import java.awt.*;
import java.util.Random;

/**
 * Created by Samuel Roberts
 * on 01/02/12
 */
public class Constants {
    public static int screenWidth = 1024;
    public static int screenHeight = 768;

    public static int delay = 20;
    public static double dt = delay / 1000.0;
    public static Random rand = new Random();

    //public static final int TEAM_LEFT = 0;
    //public static final int TEAM_RIGHT = 1;

    public static boolean allowFriendlyFire = false;
    public static boolean usePickups = true;

    public static int numComponents = 5;
    public static int numWeights = 6;
    public static double startingStdDev = 100;
    public static double positionScale = 1;
    public static double componentScale = 0.1;
    public static double weightScale = 0.01;

    public static double defaultThrust = 1000;
    public static double defaultFireVel = 10000;
    public static double defaultProjectileHarm = 10;
    public static double projectileLifetime = 3.0; // seconds
    public static double maximumHull = 100;
    public static double minimumHull = 10;
    public static double maximumFuel = 10000;
    public static double forceProducedPerFuelUnit = 1000;
    public static int maximumBullets = 100;

    public static int numPickups = 1;
    public static int numMines = 1;
    public static int pickupPlacementSeed = 2063328942;
    public static double hullPickupAmount = 30;
    public static double ammoPickupAmount = 20;
    public static double fuelPickupAmount = 500;

    public static double friction = 0.99;
//    public static double friction = 1.0;
    public static double angleFriction = 0.00;
    public static double edgeBounceLoss = 0.8;

    public static int numEvals = 10000;
    public static int numShips = 10;
    public static int combatRepeats = 1;

    public static double thrusterRadiusLimit = 0;
    public static int timesteps = 1000;
//    public static int timesteps = 500;

    //public static Rectangle leftTeamStartRect = new Rectangle(0, 0, 450, 768);
    //public static Rectangle rightTeamStartRect = new Rectangle(574, 0, 450, 768);
    public static Rectangle startRect = new Rectangle(0, 0, 1024, 768);

    //public static double teamScoreWeight = 0.1;
    public static double weaponCooldown = 0.4;
    public static double mineDamageAmount = 50;

    public static double hitReward = 10;
    public static double killReward = 1000;
    public static double pickupReward = 1000;
    public static double minePenalty = 500;
    public static double bulletPenaltyMul = 1;
    public static double hullRewardMul = 10;


    public static Vector2d predatorStartPos = new Vector2d(500, 600);
    public static Vector2d preyStartPos = new Vector2d(700, 300);

    public static SimpleAction[] actions;
    static {
        actions = new SimpleAction[]{
                new SimpleAction(0, -1),
                new SimpleAction(0, 0),
                new SimpleAction(0, 1),
                new SimpleAction(1, -1),
                new SimpleAction(1, 0),
                new SimpleAction(1, 1),
        };
    }

    public static int macroActionStep = 30;
    public static int nIts = 1000;
//    public static int nIts = 200;
    public static int rolloutDepth = 100;
    public static int asteroidPlacementSeed = 106839;



    public static enum WorldType {
        BOUNDED,
        WRAPPING,
        CIRCULAR
    }
//    public static WorldType worldType = WorldType.BOUNDED;
//    public static WorldType worldType = WorldType.WRAPPING;
    public static WorldType worldType = WorldType.CIRCULAR;

    public static int numAsteroids = 3;
    public static double minAsteroidRadius = 5;
    public static double maxAsteroidRadius = 15;
    public static double maxVelocityRange = 20;

    public static boolean drawVelocities = false;
    public static boolean drawPredictedPoint = true;

    public static boolean useGreedyInternalModel = false;

    public static boolean usePredictedPreyPos = true;

    public static enum HumanControl {
        NONE,
        PREDATOR,
        PREY
    }
    public static HumanControl humanControl = HumanControl.NONE;
//    public static HumanControl humanControl = HumanControl.PREDATOR;
//    public static HumanControl humanControl = HumanControl.PREY;
}
