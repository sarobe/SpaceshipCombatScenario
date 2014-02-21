package controller.basic;

import common.Constants;
import controller.StateController;
import controller.mcts.gamestates.IGameState;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */

public class RandomController extends StateController {

    public RandomController(Spaceship ship) {
        super(ship);
    }

    public RandomController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
    }

    @Override
    public void think() {
        if (!terminal) {
            IGameState gameState = constructState();
            if (gameState.isTerminal()) {
                terminal = true;
            } else {
                if (timesteps % Constants.macroActionStep == 0) {
                    currentAction = Constants.rand.nextInt(Constants.actions.length);
                }
                // use current action
                useSimpleAction(ship, currentAction);
            }
            bestPredictedScore = gameState.value();
        }
        super.think();
    }
}



