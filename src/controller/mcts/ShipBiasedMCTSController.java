package controller.mcts;

import common.Constants;
import common.math.Vector2d;
import controller.Controller;
import controller.ShipState;
import controller.StateController;
import controller.mcts.gamestates.IGameState;
import controller.mcts.gamestates.PickupGameState;
import controller.mcts.gamestates.PredatorPreyGameState;
import ea.FitVectorSource;
import problem.PickupManager;
import problem.ProjectileManager;
import spaceship.Spaceship;
import spaceship.SimObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 * with some heavy borrowings from Simon Lucas
 */
public class ShipBiasedMCTSController extends StateController {

    ITunableRoller roller;
    FitVectorSource source;
    TreeNodeLite lastSearch;

    public ShipBiasedMCTSController(Spaceship ship) {
        super(ship);
        init();
    }

    public ShipBiasedMCTSController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
        init();
    }

    private void init() {
        roller = new FeatureWeightedRoller(constructState());
        source = new RandomMutationHillClimberHack(roller.nDim(), 1);
        //think();
    }

    @Override
    public void think() {
        if(!terminal) {
            if(timesteps % Constants.macroActionStep == 0) {
                //if(timesteps < Constants.timesteps) currentAction = getAction(constructState());
                //else currentAction = 1; // do _nothing_

                currentAction = getAction(constructState());
            }

            // use current action
            useSimpleAction(ship, currentAction);
        }
        super.think();
    }

    public int getAction(IGameState state) {
        TreeNodeLite tn = new TreeNodeLite(roller);
        tn.mctsSearch(state, Constants.nIts, roller, source);
        if(!tn.isLeaf()) {
            bestPredictedScore = 0;
            for(TreeNodeLite child : tn.children) {
                if(child != null) {
                    //System.out.println(child.action + ": " + child.meanValue());
                    if(child.meanValue() > bestPredictedScore) bestPredictedScore = child.meanValue();
                }
            }
        } else {
            //System.out.println(tn.meanValue());
            //System.out.println("Terminal: " + tn.meanValue());
            //System.out.println("Ended at timestep " + timesteps);
            terminal = true;
        }
        //System.out.println(Arrays.toString(source.bestVec()));
        //System.out.println();
        lastSearch = tn;
        int action = 0;
        if(!terminal) action = tn.bestRootAction(state, roller);
        return action;
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        // draw rollouts
        if(lastSearch != null) {
            drawAllRollouts(g, lastSearch);
        }
    }

    private void drawAllRollouts(Graphics2D g, TreeNodeLite rootNode) {
        if(rootNode == null) return;
        ShipState initialShipState = new ShipState(ship);

        if(!rootNode.isLeaf()) {
            for(TreeNodeLite node : rootNode.children) {
                drawAllRollouts(g, node);
            }
        }
        drawRollouts(g, rootNode);
        ship.setState(initialShipState);
    }

    private void drawRollouts(Graphics2D g, TreeNodeLite node) {
        IGameState initialState = node.initialState;
        ShipState currentShipState = initialState.getShipState();

        for(RollOut rollOut : node.rollOuts) {
            ship.setState(currentShipState);
            ShipState prevState = currentShipState;
            ShipState nextState = null;
            for(Integer action : rollOut.actions) {
                useSimpleAction(ship, action);
                for(int i=0; i<Constants.macroActionStep; i++) {
                    ship.update();
                }
                nextState = new ShipState(ship);

                // draw states
                // calculate the relative value on a 0 - 1 scale of the value of this action
                double value = (rollOut.value - node.worstRolloutValue) / (node.bestRolloutValue - node.worstRolloutValue);
                g.setColor(Color.getHSBColor(1.0f, 0.0f, (float)value));
                g.drawLine((int) prevState.px, (int) prevState.py, (int) nextState.px, (int) nextState.py);

                prevState = nextState;
            }
        }
    }
}


