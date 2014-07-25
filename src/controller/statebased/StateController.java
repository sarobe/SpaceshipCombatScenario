package controller.statebased;

import common.Constants;
import controller.Controller;
import controller.ShipState;
import controller.gamestates.IGameState;
import controller.gamestates.PickupGameState;
import controller.gamestates.PredatorPreyGameState;
import problem.managers.PickupManager;
import problem.managers.ProjectileManager;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.util.List;

/**
 * Created by Samuel Roberts, 2014
 */
public abstract class StateController extends Controller {

    // A common class for state-based controllers.
    // Remember to adjust what states are constructed! TODO: figure out a better way to vary what states are constructed.

    protected int currentAction;

    public double bestPredictedScore = 0;

    public boolean terminal = false;

    public StateController(Spaceship ship) {
        super(ship);
    }

    public StateController(Spaceship ship, Spaceship antagonist, boolean flag) {
        super(ship, antagonist, flag);
    }

    public void think(List<SimObject> ships) {
        think();
    }

    public void think() {
        super.think();
        terminal = constructState().isTerminal();
        if(terminal) bestPredictedScore = constructState().value();
    }



    public IGameState constructState() {
        IGameState currentState;
        ProjectileManager.suppressNewProjectiles(true);

        ShipState initialState = new ShipState(ship);

        //currentState = constructPickupGameState();
        currentState = constructPredatorPreyGameState();
        //currentState = new PickupGameState(ship, new ShipState(ship), 0, PickupManager.getPickupStates());

        // reset ship
        ship.setState(initialState);
        ProjectileManager.suppressNewProjectiles(false);
        return currentState;
    }

    private IGameState constructPredatorPreyGameState() {
        return new PredatorPreyGameState(ship, new ShipState(ship), antagonist, new ShipState(antagonist), timesteps, isPredator);
    }

    private IGameState constructPickupGameState() {
        return new PickupGameState(ship, new ShipState(ship), timesteps, PickupManager.getPickupStates());
    }

    public static void useSimpleAction(Spaceship ship, int action) {
        ship.useAction(action);
    }

    public double getScore() {
        return constructState().value();
    }

    public static double getScoreForPredatorPrey(Spaceship ship, Spaceship antagonist, boolean isPredator, int timesteps) {
        IGameState gameState = new PredatorPreyGameState(ship, new ShipState(ship), antagonist, new ShipState(antagonist), timesteps, isPredator);
        boolean temp = Constants.usePredictedPreyPos;
        Constants.usePredictedPreyPos = false;
        double value = gameState.value();
        Constants.usePredictedPreyPos = temp;
        return value;
    }

}
