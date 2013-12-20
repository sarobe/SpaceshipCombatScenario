package controller.singleMCTS;

import controller.mcts.IGameState;

/**
 * Created by IntelliJ IDEA.
 * User: diego
 * Date: 12/02/13
 * Time: 12:53
 * To change this template use File | Settings | File Templates.
 */
public interface Player {
    void init();
    int run(IGameState a_gameState, long a_timeDue, boolean a);
    void reset();
    Heuristic getHeuristic();
    PlayoutInfo getPlayoutInfo();
}
