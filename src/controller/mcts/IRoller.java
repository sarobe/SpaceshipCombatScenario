package controller.mcts;

import controller.mcts.gamestates.IGameState;

/**
 * Created by Samuel Roberts, 2013
 */
public interface IRoller {
    int roll(IGameState gameState);
}
