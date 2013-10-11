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

    public static final int TEAM_LEFT = 0;
    public static final int TEAM_RIGHT = 1;

    public static int numComponents = 5;
    public static int numWeights = 4;
    public static double startingStdDev = 100;
    public static double componentScale = 0.1;
    public static double weightScale = 0.01;

    public static double defaultThrust = 400;
    public static double defaultFireVel = 6000;
    public static double defaultProjectileHarm = 10;
    public static double projectileLifetime = 3.0; // seconds
    public static double maximumHull = 200;
    public static double minimumHull = 10;
    public static double maximumFuel = 200000;

    public static double friction = 1.00;
    public static double edgeBounceLoss = 0.8;

    public static int numEvals = 10000;

    public static double thrusterRadiusLimit = 15;
    public static int timesteps = 300;

    public static Rectangle leftTeamStartRect = new Rectangle(0, 0, 450, 768);
    public static Rectangle rightTeamStartRect = new Rectangle(574, 0, 450, 768);

    public static double teamScoreWeight = 0.1;
    public static double weaponCooldown = 0.4;
}
