package controller.mcts;

import controller.Controller;
import controller.ShipState;
import problem.Pickup;
import problem.PickupManager;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 * with some heavy borrowings from Simon Lucas
 */
public class ShipMCTSController extends Controller {

    static int nIts = 100;
    public static int macroActionStep = 20;
    int timesteps;
    int currentAction;
    IRoller roller;


    public ShipMCTSController(Spaceship ship) {
        super(ship);
        roller = new BasicRoller();
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
            ShipState initialState = new ShipState(ship);

            Map<Pickup, Boolean> pickupStates = new HashMap<Pickup, Boolean>();
            for(Pickup p : PickupManager.pickupList) {
                pickupStates.put(p, !p.alive);
            }

            IGameState currentState = new PickupGameState(ship, new ShipState(ship), timesteps, pickupStates);
            currentAction = getAction(currentState);

            // reset ship
            ship.setState(initialState);
        }
        timesteps++;


        // use action
        binaryToActions(ship, currentAction);
    }

    public int getAction(IGameState state) {
        TreeNodeLite tn = new TreeNodeLite(roller);
        tn.mctsSearch(state, nIts);
        if(tn.nonTerminal()) return tn.bestRootAction(state, roller);
        else return 0;
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();
        g.translate(ship.pos.x, ship.pos.y);
        g.setColor(Color.YELLOW);
        g.fillOval(-5, -5, 10, 10);
        g.setTransform(at);
    }

}
