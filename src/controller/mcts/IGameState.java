package controller.mcts;

/**
 * Created by Samuel Roberts, 2013
 */
public interface IGameState {

    public boolean isTerminal();
    public double value();
    public int nActions();
    public IGameState next(int action);
    public IGameState copy();
    public double heuristicValue();

    double[] getFeatures();
}