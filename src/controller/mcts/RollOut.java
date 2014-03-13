package controller.mcts;

import common.math.Vector2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RollOut {

    public List<Integer> actions;
    public List<Vector2d> positions;
    public double value;

    public RollOut() {
        actions = new ArrayList<Integer>();
        positions = new ArrayList<Vector2d>();
    }

    public void addAction(int a) {
        actions.add(a);
    }

    public void addPosition(Vector2d pos) {
        positions.add(pos);
    }

    public void setScore(double v) {
        value = v;
    }

    public String toString() {
        return "Rollout value: " + value + "\nActions: " + Arrays.toString(actions.toArray());
    }
}
