package controller.mcts;

import common.Constants;
import common.math.Vector2d;
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
public class PickupGameState implements IGameState {

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
            // return the proximity to closest pickup (10/distance) as well as a fixed bonus for each collected pickup.
            Pickup chosen = getClosestPickup();
            double proximityScore = 100 / chosen.pos.dist(shipState.pos);
            double collectionScore =  getCollectedPickups() * 100;
            double minePenalty = getCollectedMines() * 100;
            return proximityScore + collectionScore - minePenalty;
        } else {
            return (Constants.timesteps - timestepsElapsed) * 100 - getCollectedMines() * 100;
        }
    }

    @Override
    public int nActions() {
        return ShipBiasedMCTSController.actions.length;    // Constants.numComponents;
    }

    @Override
    public IGameState next(int action) {
        ShipState initialState = new ShipState(ship);
        ship.setState(shipState);
        ProjectileManager.suppressNewProjectiles(true);
        //binaryToActions(ship, action);
//        for(int i=0; i<ship.components.size(); i++) {
//            if(i == action) ship.components.get(i).active = true;
//            else ship.components.get(i).active = false;
//        }
        ShipBiasedMCTSController.useSimpleAction(ship, action);
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

    @Override
    public double[] getFeatures() {
        // get closest living pickup
        Pickup chosen = getClosestPickup();

        // use dot product features
        Vector2d s = chosen.pos.copy().subtract(shipState.pos);
        Vector2d forward = ship.forwardDir;
        Vector2d left = forward.copy();
        left.rotate(Math.PI/2);
        Vector2d right = forward.copy();
        right.rotate(-Math.PI/2);

        // calculate the features
        double f = forward.scalarProduct(s);
        double l = left.scalarProduct(s);
        double r = right.scalarProduct(s);

        // feature four: velocity divided by distance (becomes larger as velocity increases and distance decreases)
        // negated because the larger this number is, the worse scenario it is
        //double vs = -(ship.v.mag()/s.mag());

        return new double[]{f,l,r};
    }

    public Pickup getClosestPickup() {
        double closestDist = Double.MAX_VALUE;
        Pickup chosen = null;
        for(Pickup p : pickupStates.keySet()) {
            if(p.type != PickupType.MINE && pickupStates.get(p)) {
                double dist = p.pos.dist(shipState.pos);
                if(dist < closestDist) {
                    closestDist = dist;
                    chosen = p;
                }
            }
            if(chosen == null) chosen = p;
        }
        return chosen;
    }

//    protected void binaryToActions(Spaceship target, int encodedActions) {
//        int j = 1;
//        int actionNum = 0;
//        int totalPossibleActions = target.components.size();
//        while(actionNum < totalPossibleActions) {
//            if((encodedActions & j) != 0) {
//                target.components.get(actionNum).active = true;
//            } else {
//                target.components.get(actionNum).active = false;
//            }
//            actionNum++;
//            j *= 2;
//        }
//    }
}
