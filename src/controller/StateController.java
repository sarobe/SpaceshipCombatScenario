package controller;

import common.math.Vector2d;
import controller.gamestates.IGameState;
import controller.gamestates.PickupGameState;
import controller.gamestates.PredatorPreyGameState;
import problem.PickupManager;
import problem.ProjectileManager;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */
public abstract class StateController extends Controller {

    // A common class for state-based controllers.
    // Remember to adjust what states are constructed! TODO: figure out a better way to vary what states are constructed.

    protected int timesteps;
    protected int currentAction;

    public double bestPredictedScore = 0;

    public List<Vector2d> trace;

    public boolean terminal = false;

    public Spaceship antagonist; // for predator vs prey
    public boolean isPredator = true; // for predator vs prey


    public StateController(Spaceship ship) {
        super(ship);
        trace = new ArrayList<Vector2d>();
        timesteps = 0;
    }

    public StateController(Spaceship ship, Spaceship antagonist, boolean flag) {
        this(ship);
        this.antagonist = antagonist;
        isPredator = flag;
    }

    public void think(List<SimObject> ships) {
        think();
    }

    public void think() {
        trace.add(new Vector2d(ship.pos));
        terminal = constructState().isTerminal();
        if(terminal) bestPredictedScore = constructState().value();
        timesteps++;
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        g.translate(ship.pos.x, ship.pos.y);
        g.setColor(Color.YELLOW);
        g.drawOval((int)(-ship.radius), (int)(-ship.radius), (int)(ship.radius*2), (int)(ship.radius*2));

        Vector2d shipForward = ship.getForward();
        g.drawLine(0, 0, (int)(shipForward.x * 20), (int)(shipForward.y * 20));

        g.setTransform(at);

        // draw ship trail
        if(isPredator) g.setColor(Color.BLUE);
        else g.setColor(Color.ORANGE);

        Vector2d lastP = trace.get(0);
        for(Vector2d p : trace) {
            g.drawLine((int)p.x, (int)p.y, (int)lastP.x, (int)lastP.y);
            lastP = p;
        }
    }

    public IGameState constructState() {
        IGameState currentState;
        ProjectileManager.suppressNewProjectiles(true);

        ShipState initialState = new ShipState(ship);

        //currentState = constructPickupGameState();
        currentState = constructPredatorPreyGameState();
        //currentState = new PickupGameState(ship, new ShipState(ship), 0, PickupManager.getPickupStates());

        // reset ship
        ship.setState(initialState);
        ProjectileManager.suppressNewProjectiles(false);
        return currentState;
    }

    private IGameState constructPredatorPreyGameState() {
        return new PredatorPreyGameState(ship, new ShipState(ship), antagonist, new ShipState(antagonist), timesteps, isPredator);
    }

    private IGameState constructPickupGameState() {
        return new PickupGameState(ship, new ShipState(ship), timesteps, PickupManager.getPickupStates());
    }

    public static void useSimpleAction(Spaceship ship, int action) {
        ship.useAction(action);
    }

    public double getScore() {
        return constructState().value();
    }

}
