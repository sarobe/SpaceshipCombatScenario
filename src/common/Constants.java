package common;

import common.math.Vector2d;

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
    public static int numWeights = 4;
    public static double startingStdDev = 100;
    public static double positionScale = 1;
    public static double componentScale = 0.1;
    public static double weightScale = 0.01;

    public static double defaultThrust = 1000;
    public static double defaultFireVel = 20000;
    public static double defaultProjectileHarm = 5;
    public static double projectileLifetime = 3.0; // seconds
    public static double maximumHull = 200;
    public static double minimumHull = 10;
    public static double maximumFuel = 10000;
    public static double forceProducedPerFuelUnit = 1000;
    public static int maximumBullets = 100;

    public static double numPickups = 20;
    public static int pickupPlacementSeed = 2156;
    public static double hullPickupAmount = 30;
    public static double ammoPickupAmount = 20;
    public static double fuelPickupAmount = 500;

    public static double friction = 0.99;
    public static double angleFriction = 0.00;
    public static double edgeBounceLoss = 0.8;

    public static int numEvals = 10000;
    public static int combatRepeats = 5;

    public static double thrusterRadiusLimit = 0;
    public static int timesteps = 300;

    //public static Rectangle leftTeamStartRect = new Rectangle(0, 0, 450, 768);
    //public static Rectangle rightTeamStartRect = new Rectangle(574, 0, 450, 768);
    public static Rectangle startRect = new Rectangle(0, 0, 1024, 768);

    //public static double teamScoreWeight = 0.1;
    public static double weaponCooldown = 0.4;
    public static double mineDamageAmount = 60;
}
