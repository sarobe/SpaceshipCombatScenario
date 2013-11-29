package controller.mcts;

import common.Constants;
import controller.Controller;
import controller.ShipState;
import ea.FitVectorSource;
import problem.Pickup;
import problem.PickupManager;
import problem.ProjectileManager;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samuel Roberts, 2013
 * with some heavy borrowings from Simon Lucas
 */
public class ShipBiasedMCTSController extends Controller {

    static int nIts = 200;
    public static int macroActionStep = 50;
    int timesteps;
    int currentAction;
    ITunableRoller roller;
    FitVectorSource source;

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


    public ShipBiasedMCTSController(Spaceship ship) {
        super(ship);
        roller = new FeatureWeightedRoller(constructState());
        source = new RandomMutationHillClimberHack(roller.nDim(), 1);
        think(); // get the starting action to use
        timesteps = 0;
    }

    @Override
    public void think(List<SimObject> ships) {
        // not used at the moment
        think();
    }

    @Override
    public void think() {
        // condense information about pickups into a map of pickup states
        if(timesteps % macroActionStep == 0) {
            if(timesteps < Constants.timesteps) currentAction = getAction(constructState());
            else currentAction = 1; // do _nothing_
        }
        timesteps++;

        // use current action
        useSimpleAction(ship, currentAction);
    }

    public int getAction(IGameState state) {
        TreeNodeLite tn = new TreeNodeLite(roller);
        tn.mctsSearch(state, nIts, roller, source);
        return tn.bestRootAction(state, roller);
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();
        g.translate(ship.pos.x, ship.pos.y);
        g.setColor(Color.YELLOW);
        g.fillOval(-5, -5, 10, 10);
        g.setTransform(at);
    }

    public IGameState constructState() {
        IGameState currentState;
        ProjectileManager.suppressNewProjectiles(true);

        ShipState initialState = new ShipState(ship);

        Map<Pickup, Boolean> pickupStates = new HashMap<Pickup, Boolean>();
        for(Pickup p : PickupManager.pickupList) {
            pickupStates.put(p, !p.alive);
        }

        currentState = new PickupGameState(ship, new ShipState(ship), timesteps, pickupStates);

        // reset ship
        ship.setState(initialState);
        ProjectileManager.suppressNewProjectiles(false);
        return currentState;
    }

    public static void useSimpleAction(Spaceship ship, int action) {
        SimpleAction simpleAction = actions[action];
        int realAction = 0;
        if(simpleAction.thrust > 0) {
            realAction |= ship.forward;
        }
        if(simpleAction.turn < 0) {
            realAction |= ship.turnCW;
        } else if(simpleAction.turn > 0) {
            realAction |= ship.turnCCW;
        }

        binaryToActions(ship, realAction);
    }

}


class SimpleAction {
    int thrust;
    int turn;

    SimpleAction(int thrust, int turn) {
        this.thrust = thrust;
        this.turn = turn;
    }


}