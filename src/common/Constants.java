package common;

import common.math.Vector2d;

import java.util.Random;

/**
 * Created by Samuel Roberts
 * on 01/02/12
 */
public class Constants {
    public static int screenWidth = 800;
    public static int screenHeight = 600;

    public static int delay = 20;
    public static double dt = delay / 1000.0;
    public static Random rand = new Random();

    public static int numComponents = 5;
    public static double defaultThrust = 200;
    public static double defaultFireVel = 4000;
    public static double defaultProjectileHarm = 10;
    public static double maximumHull = 200;

    public static double friction = 1.00;

    public static int numEvals = 10000;

    public static double thrusterRadiusLimit = 15;
    public static int timesteps = 300;

    public static Vector2d leftStartPos = new Vector2d(100, 300);
    public static Vector2d rightStartPos = new Vector2d(700, 300);
}
