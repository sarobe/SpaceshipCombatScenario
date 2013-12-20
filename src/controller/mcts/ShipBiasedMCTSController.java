package controller.mcts;

import common.Constants;
import common.math.Vector2d;
import controller.Controller;
import controller.ShipState;
import ea.FitVectorSource;
import problem.PickupManager;
import problem.ProjectileManager;
import spaceship.BasicSpaceship;
import spaceship.ComplexSpaceship;
import spaceship.Spaceship;
import spaceship.SimObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 * with some heavy borrowings from Simon Lucas
 */
public class ShipBiasedMCTSController extends Controller {

    static int nIts = 1000;
    public static int macroActionStep = 15;
    public static int rolloutDepth = 50;
    int timesteps;
    int currentAction;
    ITunableRoller roller;
    FitVectorSource source;
    TreeNodeLite lastSearch;

    boolean terminal = false;

    public double bestPredictedScore = 0;

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
        if(!terminal) {
            if(timesteps % macroActionStep == 0) {
                //if(timesteps < Constants.timesteps) currentAction = getAction(constructState());
                //else currentAction = 1; // do _nothing_

                currentAction = getAction(constructState());
            }
            timesteps++;

            // use current action
            useSimpleAction(ship, currentAction);
        }
    }

    public int getAction(IGameState state) {
        TreeNodeLite tn = new TreeNodeLite(roller);
        tn.mctsSearch(state, nIts, roller, source);
        if(!tn.isLeaf()) {
            for(TreeNodeLite child : tn.children) {
                if(child != null) {
                    System.out.println(child.action + ": " + child.meanValue());
                    if(child.meanValue() > bestPredictedScore) bestPredictedScore = child.meanValue();
                }
            }
        } else {
            System.out.println("Terminal: " + tn.meanValue());
            terminal = true;
        }
        //System.out.println(Arrays.toString(source.bestVec()));
        System.out.println();
        lastSearch = tn;
        int action = 0;
        if(!terminal) action = tn.bestRootAction(state, roller);
        return action;
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        g.translate(ship.pos.x, ship.pos.y);
        g.setColor(Color.YELLOW);
        g.fillOval(-5, -5, 10, 10);

        Vector2d shipForward = ship.getForward();
        g.drawLine(0, 0, (int)(shipForward.x * 20), (int)(shipForward.y * 20));

        g.setTransform(at);


        if(lastSearch != null) {
            drawRollouts(g, lastSearch);
        }
    }

    private void drawRollouts(Graphics2D g, TreeNodeLite node) {
        IGameState initialState = node.initialState;
        ShipState initialShipState = new ShipState(ship);
        ShipState currentShipState = initialState.getShipState();

        for(RollOut rollOut : node.rollOuts) {
            ship.setState(currentShipState);
            ShipState prevState = currentShipState;
            ShipState nextState = null;
            for(Integer action : rollOut.actions) {
                useSimpleAction(ship, action);
                for(int i=0; i<macroActionStep; i++) {
                    ship.update();
                }
                nextState = new ShipState(ship);

                // draw states
                g.setColor(Color.getHSBColor(1.0f, 0.0f, Math.max((float)(rollOut.value/node.bestRolloutValue), 0.0f)));
                g.drawLine((int) prevState.px, (int) prevState.py, (int) nextState.px, (int) nextState.py);

                prevState = nextState;
            }
        }
        ship.setState(initialShipState);
    }

    public IGameState constructState() {
        IGameState currentState;
        ProjectileManager.suppressNewProjectiles(true);

        ShipState initialState = new ShipState(ship);

        //currentState = new PickupGameState(ship, new ShipState(ship), timesteps, PickupManager.getPickupStates());
        currentState = new PickupGameState(ship, new ShipState(ship), 0, PickupManager.getPickupStates());

        // reset ship
        ship.setState(initialState);
        ProjectileManager.suppressNewProjectiles(false);
        return currentState;
    }

    public static void useSimpleAction(Spaceship ship, int action) {
        ship.useAction(action);
    }

}


