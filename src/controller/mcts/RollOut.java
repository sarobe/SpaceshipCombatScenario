package controller.mcts;

import common.math.Vector2d;
import controller.gamestates.IGameState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RollOut {

    public List<Integer> actions;
    public List<Vector2d> positions;
    public List<Vector2d> velocities;
    public List<IGameState> gameStates;
    public double value;

    public RollOut() {
        actions = new ArrayList<Integer>();
        positions = new ArrayList<Vector2d>();
        velocities = new ArrayList<Vector2d>();
        gameStates = new ArrayList<IGameState>();
    }

    public void addAction(int a) {
        actions.add(a);
    }

    public void addPosition(Vector2d pos) {
        positions.add(pos);
    }

    public void addVelocity(Vector2d vel) {
        velocities.add(vel);
    }

    public void addGameState(IGameState state) {
        gameStates.add(state);
    }

    public void setScore(double v) {
        value = v;
    }

    public String toString() {
        return "Rollout value: " + value + "\nActions: " + Arrays.toString(actions.toArray());
    }
}
