package controller.singleMCTS;

import controller.gamestates.IGameState;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:16
 */
public interface Heuristic
{
    public double value(IGameState a_gameState);
    public double[] getValueBounds();
    public boolean mustBePruned(IGameState a_newGameState, IGameState a_previousGameState);
    public void setPlayoutInfo(PlayoutInfo a_pi);
    public void addPlayoutInfo(int a_lastAction, IGameState a_gameState);
}
