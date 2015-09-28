package controller.mcts;

import common.Constants;
import common.math.Vector2d;
import controller.ShipState;
import controller.statebased.StateController;
import controller.gamestates.IGameState;
import ea.FitVectorSource;
import spaceship.Spaceship;

import java.awt.*;

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
        if(Constants.drawControllerDetails && lastSearch != null) {
            drawAllRollouts(g, lastSearch, lastSearch.initialState.copy());
        }
    }

    private void drawAllRollouts(Graphics2D g, TreeNodeLite rootNode, IGameState initState) {
        if(rootNode == null) return;
        ShipState initialShipState = new ShipState(ship);
        if(!rootNode.isLeaf()) {
            for(TreeNodeLite child : rootNode.children) {
                drawAllRollouts(g, child, rootNode.initialState);
            }
        }
        drawRollouts(g, rootNode, initState);
        ship.setState(initialShipState);
    }

    private void drawRollouts(Graphics2D g, TreeNodeLite node, IGameState initState) {
        IGameState initialState = node.initialState;

        for(RollOut rollOut : node.rollOuts) {
            Vector2d prevPos = initialState.getShipState().pos;
            Vector2d nextPos = null;
            for(int i = 0; i < rollOut.actions.size(); i++) {
                Vector2d pos = rollOut.positions.get(i);
                Vector2d vel = rollOut.velocities.get(i);
                nextPos = pos;

                // draw states
                // if the line is too big (the rollout wraps to the other side of the screen) don't draw it
                // TODO: DO ANYTHING MORE EFFICIENT THAN HAVING A DISTANCE CHECK FOR EVERY SINGLE LINE SEGMENT
                if(nextPos.dist(prevPos) < Constants.screenWidth/2) {
                    float value = (float)rollOut.value;
                    value = Math.max(value, 0);
                    value = Math.min(value, 1);
                    g.setColor(new Color(value, value, value));
                    g.drawOval((int) prevPos.x - 2, (int) prevPos.y - 2, 4, 4);
                    g.drawLine((int) prevPos.x, (int) prevPos.y, (int) nextPos.x, (int) nextPos.y);

                    if(Constants.drawVelocities) {
                        // draw velocity, low velocity red, high velocity purple
                        value = (float)(vel.mag()/1000);
                        if(value > 1) value = 1;
                        g.setColor(Color.getHSBColor(value, 1.0f, 1.0f));
                        g.drawLine((int) (pos.x), (int) (pos.y), (int) (pos.x + vel.x), (int) (pos.y + vel.y));
                    }

                    if(Constants.drawPredictedPoint) {
                        // draw predicted point
                        Vector2d predictedPoint = rollOut.gameStates.get(i).getShipState().predictedPoint;
                        if(predictedPoint != null) {
                            g.setColor(Color.RED);
                            //g.drawLine((int) (pos.x), (int) (pos.y), (int) (predictedPoint.x), (int) (predictedPoint.y));
                            g.drawOval((int)(predictedPoint.x - 5), (int)(predictedPoint.y - 5), 10, 10);
                        }
                    }

                }

                prevPos = nextPos;
            }
        }
    }
}


