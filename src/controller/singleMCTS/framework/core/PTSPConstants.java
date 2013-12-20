package controller.singleMCTS.framework.core;

/**
 * This class contains some important constants for the game.
 * PTSP-Competition
 * Created by Diego Perez, University of Essex.
 * Date: 19/12/11
 */
public abstract class PTSPConstants
{
    /**
     * Delay between time steps, used for replays and human plays.
     * It is set to 16ms, what implies near 62fps (1000/16) = 62.5
     */
    public final static int DELAY = 16;

    /**
     * Time constant
     */
    public final static double T = 1.0;

    /**
     * This is the number of steps allowed until reaching the next waypoint.
     */
    private final static int STEPS_PER_WAYPOINT = 1000;

    /**
     * The velocity of the ship will be multiplied by this amount when colliding with a wall.
     */
    public final static double COLLISION_SPEED_RED = 0.25;

    /**
     * The velocity of the ship will be multiplied by this amount when colliding with a DAMAGE wall.
     */
    public final static double COLLISION_DAMAGE_SPEED_RED = 0.1;

    /**
     * The velocity of the ship will be multiplied by this amount when colliding with an ELASTIC wall.
     */
    public final static double COLLISION_ELASTIC_SPEED_RED = 0.9;

    /**
     * Time for the controller to be initialized.
     */
    private final static int INIT_TIME_MS = 100;        //To be multiplied by num. waypoints.

    /**
     * Time for the controller to provide an action every step.
     */
    public final static int ACTION_TIME_MS = 40;

    /**
     * If the controller spends more than TIME_ACTION_DISQ to reply with an action,
     * it gets disqualified from this game (getting 0 wp and getStepsPerWaypoints() time steps.)
     */
    public final static int TIME_ACTION_DISQ = ACTION_TIME_MS * 3;

}
