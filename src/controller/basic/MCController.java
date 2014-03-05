package controller.basic;

import common.Constants;
import common.utilities.Picker;
import controller.ShipState;
import controller.StateController;
import controller.mcts.RollOut;
import controller.mcts.gamestates.IGameState;
import spaceship.Spaceship;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */
public class MCController extends StateController {

    List<RollOut> lastRollouts;
    IGameState initialState;
    double worstRolloutValue = 0;

    public MCController(Spaceship ship) {
        super(ship);
    }

    public MCController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
    }

    @Override
    public void think() {
        if (!terminal) {
            if (timesteps % Constants.macroActionStep == 0) {
                currentAction = getAction(constructState());
            }
            // use current action
            useSimpleAction(ship, currentAction);
        }
        super.think();
    }

    public int getAction(IGameState state) {
        // for each action, get the value
        Picker<Integer> actionPicker = new Picker<Integer>();
        List<IGameState> futureStates = new ArrayList<IGameState>();

        int numTrials = Constants.nIts / state.nActions();

        lastRollouts = new ArrayList<RollOut>();
        initialState = state.copy();
        worstRolloutValue = Double.MAX_VALUE;

        for(int i = 0; i<state.nActions(); i++) {
            IGameState nextState = state.copy().next(i);
            double score = 0;
            for(int j=0; j<numTrials; j++) {
                // copy the next state as a base
                IGameState rolloutState = nextState.copy();
                RollOut ro = new RollOut();
                ro.addAction(i);
                // do a random rollout for the depth of the rollout
                for(int t=0; t<Constants.rolloutDepth; t += Constants.macroActionStep) {
                    int randAct = Constants.rand.nextInt(state.nActions());
                    rolloutState.next(randAct);
                    ro.addAction(randAct);
                    if(rolloutState.isTerminal()) break;
                }
                double finalValue = rolloutState.value();
                score += finalValue;
                //System.out.println("Action " + i + " Trial " + j + ": Got score " + finalValue + " (cumulative " + score + ")");
                ro.setScore(finalValue);
                if(finalValue < worstRolloutValue) worstRolloutValue = finalValue;
                lastRollouts.add(ro);
            }
            //System.out.println("Action " + i + " Final Score: " + score/(double)numTrials);
            actionPicker.add(score/(double)numTrials, i);
            futureStates.add(nextState);
        }

        int action = actionPicker.getBest();
        //System.out.println("Picked action " + action);
        if(futureStates.get(action).isTerminal()) terminal = true;
        bestPredictedScore = actionPicker.getBestScore();
        return actionPicker.getBest();
    }


    public void draw(Graphics2D g) {
        super.draw(g);

        // draw rollouts
        if(lastRollouts != null) {
            drawRollouts(g, initialState);
        }
    }


    private void drawRollouts(Graphics2D g, IGameState initialState) {
        ShipState currentShipState = new ShipState(ship);
        ShipState initialShipState = initialState.getShipState();

        for(RollOut rollOut : lastRollouts) {
            ship.setState(initialShipState);
            ShipState prevState = initialShipState;
            ShipState nextState = null;
            for(Integer action : rollOut.actions) {
                useSimpleAction(ship, action);
                for(int i=0; i<Constants.macroActionStep; i++) {
                    ship.update();
                }
                nextState = new ShipState(ship);

                // draw states
                // calculate the relative value on a 0 - 1 scale of the value of this action
                // no longer needed to calculate the relative value, the value can ONLY BE 0 to 1
                float value = (float)rollOut.value;
                g.setColor(new Color(value, value, value));
                g.drawLine((int) prevState.px, (int) prevState.py, (int) nextState.px, (int) nextState.py);

                prevState = nextState;
            }
        }

        ship.setState(currentShipState);
    }

}
