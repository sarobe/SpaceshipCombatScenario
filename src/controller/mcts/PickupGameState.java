package controller.mcts;

import common.Constants;
import controller.ShipState;
import problem.Pickup;
import problem.PickupManager;
import problem.PickupType;
import problem.ProjectileManager;
import spaceship.SimObject;
import spaceship.Spaceship;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samuel Roberts, 2013
 */
public class PickupGameState  implements IGameState {

    // a map of pickup states
    // true if the pickup has been collected
    // false otherwise
    int timestepsElapsed;
    Map<Pickup, Boolean> pickupStates;
    Spaceship ship;
    ShipState shipState;

    public PickupGameState(Spaceship ship, ShipState shipState, int timestepsElapsed, Map<Pickup, Boolean> pickupStates) {
        this.ship = ship;
        this.shipState = shipState;
        this.pickupStates = new HashMap<Pickup, Boolean>(pickupStates);
        this.timestepsElapsed = timestepsElapsed;
    }

    private int getCollectedPickups() {
        int numCollected = 0;
        for(Pickup p : pickupStates.keySet()) {
            if(p.type != PickupType.MINE && pickupStates.get(p)) {
                numCollected++;
            }
        }
        return numCollected;
    }

    private int getCollectedMines() {
        int numCollected = 0;
        for(Pickup p : pickupStates.keySet()) {
            if(p.type == PickupType.MINE && pickupStates.get(p)) {
                numCollected++;
            }
        }
        return numCollected;
    }

    @Override
    public boolean isTerminal() {
        return (!shipState.alive) || (getCollectedPickups() == PickupManager.getTotalPickups()) || (timestepsElapsed >= Constants.timesteps);
    }

    @Override
    public double value() {
        if(!shipState.alive) {
            // a state that ends with the ship being dead is a bad state
            double badStateValue = -99999;
            return badStateValue;
        }
        else if(getCollectedPickups() < PickupManager.getTotalPickups()) {
            return getCollectedPickups() * 10 - getCollectedMines() * 10;
        } else {
            return (Constants.timesteps - timestepsElapsed) * 10 - getCollectedMines() * 10;
        }
    }

    @Override
    public int nActions() {
        return (int)Math.pow(2, Constants.numComponents);
    }

    @Override
    public IGameState next(int action) {
        ShipState initialState = new ShipState(ship);
        ship.setState(shipState);
        ProjectileManager.suppressNewProjectiles(true);
        binaryToActions(ship, action);
        for(int i =0; i < ShipMCTSController.macroActionStep; i++) {
            ship.update();
        }
        PickupGameState newState = new PickupGameState(ship, new ShipState(ship), timestepsElapsed + ShipMCTSController.macroActionStep, pickupStates);
        ship.setState(initialState);
        ProjectileManager.suppressNewProjectiles(false);
        return newState;
    }

    @Override
    public IGameState copy() {
        return new PickupGameState(ship, new ShipState(shipState), timestepsElapsed, pickupStates);
    }

    @Override
    public double heuristicValue() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void binaryToActions(Spaceship target, int encodedActions) {
        int j = 1;
        int actionNum = 0;
        int totalPossibleActions = target.components.size();
        while(actionNum < totalPossibleActions) {
            if((encodedActions & j) != 0) {
                target.components.get(actionNum).active = true;
            } else {
                target.components.get(actionNum).active = false;
            }
            actionNum++;
            j *= 2;
        }
    }
}
