package controller.singleMCTS;

import controller.mcts.IGameState;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:12
 */
public interface PlayoutInfo
{
    public void reset(IGameState a_gameState);
}
