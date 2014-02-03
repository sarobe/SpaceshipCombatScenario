package controller.singleMCTS;

import controller.mcts.gamestates.IGameState;
import controller.mcts.gamestates.PickupGameState;

import java.util.Arrays;

/**
 * Created by Samuel Roberts, 2013
 */
public class PlayoutPickupInfo implements PlayoutInfo {

    public IGameState startingState;
    public int[] m_playoutHistory;
    public int m_numMoves;
    public int m_numCollectedPickups;
    public int m_numCollectedMines;
    public int m_lastAction;

    public PlayoutPickupInfo()
    {
        startingState = null;
        m_playoutHistory = new int[SingleMCTSParameters.ROLLOUT_DEPTH];
        m_numMoves = 0;
        m_numCollectedPickups = 0;
        m_numCollectedMines = 0;
        m_lastAction = -1;
    }

    public void reset(IGameState a_gameState)
    {
        startingState = a_gameState.copy();
        m_playoutHistory = new int[SingleMCTSParameters.ROLLOUT_DEPTH];
        m_numMoves = 0;
        m_numCollectedPickups = ((PickupGameState)a_gameState).getCollectedPickups();
        m_numCollectedMines = ((PickupGameState)a_gameState).getCollectedMines();
        m_lastAction = -1;
    }

    public String toString() {
        String str = Arrays.toString(m_playoutHistory);
        str += ": Moves: " + m_numMoves + ", Collected Pickups: " + m_numCollectedPickups;
        return str;
    }
}
