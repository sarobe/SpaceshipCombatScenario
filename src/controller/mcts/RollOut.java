package controller.mcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RollOut {

    public List<Integer> actions;
    public double value;

    public RollOut() {
        actions = new ArrayList<Integer>();
    }

    public void addAction(int a) {
        actions.add(a);
    }

    public void setScore(double v) {
        value = v;
    }

    public String toString() {
        return "Rollout value: " + value + "\nActions: " + Arrays.toString(actions.toArray());
    }
}
