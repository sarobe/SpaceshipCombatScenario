package controller.basic;

import common.Constants;
import common.utilities.Picker;
import controller.StateController;
import controller.mcts.gamestates.IGameState;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */
public class GreedyController extends StateController {

    public GreedyController(Spaceship ship) {
        super(ship);
    }

    public GreedyController(Spaceship ship, Spaceship antagonist, boolean flag) {
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

        for(int i = 0; i<state.nActions(); i++) {
            IGameState nextState = state.copy().next(i);
            actionPicker.add(nextState.value(), i);
            futureStates.add(nextState);
        }

        int action = actionPicker.getBest();
        if(futureStates.get(action).isTerminal()) terminal = true;
        bestPredictedScore = actionPicker.getBestScore();
        return actionPicker.getBest();
    }
}
