package controller.statebased.basic;

import common.Constants;
import controller.statebased.StateController;
import controller.gamestates.IGameState;
import spaceship.Spaceship;

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



