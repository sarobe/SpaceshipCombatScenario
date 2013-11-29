package controller.mcts;

/**
 * Created by Samuel Roberts, 2013
 */
public interface ITunableRoller extends IRoller {
    int nDim();
    void setParams(double[] s);
}
