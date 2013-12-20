package controller.singleMCTS;

import controller.mcts.IGameState;
import controller.mcts.PickupGameState;

/**
 * Created by Samuel Roberts, 2013
 */
public class HeuristicPickup implements Heuristic {

    public static double HEURISTIC_WEIGHTING = 0.1;

    public PlayoutPickupInfo m_playoutInfo;
    public double[] m_bounds;

    public HeuristicPickup() {
        initBounds();
    }


    private void initBounds()
    {
        m_bounds = new double[2];
        m_bounds[0] = 0;
        m_bounds[1] = 1;
    }

    @Override
    public double value(IGameState a_gameState) {
        return a_gameState.value() + (a_gameState.heuristicValue() * HEURISTIC_WEIGHTING);
    }

    @Override
    public double[] getValueBounds() {
        return m_bounds;
    }

    @Override
    public boolean mustBePruned(IGameState a_newGameState, IGameState a_previousGameState) {
        return a_newGameState.mustBePruned();
    }

    @Override
    public void setPlayoutInfo(PlayoutInfo a_pi) {
        m_playoutInfo = (PlayoutPickupInfo)a_pi;
    }

    @Override
    public void addPlayoutInfo(int a_lastAction, IGameState a_gameState) {
        //Add action to history.
        m_playoutInfo.m_playoutHistory[m_playoutInfo.m_numMoves] = a_lastAction;
        m_playoutInfo.m_numMoves++;
        m_playoutInfo.m_lastAction = a_lastAction;

        m_playoutInfo.m_numCollectedPickups = ((PickupGameState)a_gameState).getCollectedPickups();
        m_playoutInfo.m_numCollectedMines = ((PickupGameState)a_gameState).getCollectedMines();
    }
}
