package controller.gamestates;

import common.Constants;
import common.math.Vector2d;
import controller.ShipState;
import controller.mcts.InfluenceMap;
import controller.mcts.ShipBiasedMCTSController;
import problem.entities.Pickup;
import problem.managers.PickupManager;
import problem.managers.PickupType;
import problem.managers.ProjectileManager;
import spaceship.Spaceship;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Samuel Roberts, 2013
 */
public class PickupGameState implements IGameState {

    // a map of pickup states
    // true if the pickup has been collected
    // false otherwise
    int timestepsElapsed;
    int depth;
    int bounces;
    Map<Pickup, Boolean> pickupStates;
    Spaceship ship;
    ShipState shipState;

    static final int BOUNCE_PENALTY = 1000;



    public PickupGameState(Spaceship ship, ShipState shipState, int timestepsElapsed, Map<Pickup, Boolean> pickupStates) {
        this.ship = ship;
        this.shipState = shipState;
        this.pickupStates = new HashMap<Pickup, Boolean>(pickupStates);
        this.timestepsElapsed = timestepsElapsed;
        bounces = 0;
        depth = 0;
        if(InfluenceMap.getMap() == null) InfluenceMap.createInfluenceMap(pickupStates);
    }

    public int getCollectedPickups() {
        int numCollected = 0;
        for(Pickup p : pickupStates.keySet()) {
            if(p.type != PickupType.MINE && pickupStates.get(p)) {
                numCollected++;
            }
        }
        return numCollected;
    }

    public int getCollectedMines() {
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
        return (!shipState.alive) || (getCollectedPickups() == PickupManager.getTotalPickups()) || (depth > Constants.rolloutDepth) || (timestepsElapsed >= Constants.timesteps);
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
            double proximityScore = 1000 / chosen.pos.dist(shipState.pos);
            double collectionScore =  getCollectedPickups() * 10;
            double minePenalty = getCollectedMines() * 10;
            return proximityScore + collectionScore - minePenalty;
        } else {
            return (Constants.timesteps - timestepsElapsed) - getCollectedMines() * 10;
        }
    }

    @Override
    public int nActions() {
        return Constants.actions.length;    // Constants.numComponents;
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
        Map<Pickup, Boolean> oldPickupStates = PickupManager.getPickupStates();
        ShipBiasedMCTSController.useSimpleAction(ship, action);
        for(int i =0; i < Constants.macroActionStep; i++) {
            ship.update();
            timestepsElapsed++;
            depth++;
            if(ship.bounced) bounces++;
        }
        pickupStates = PickupManager.getPickupStates();
        PickupManager.setPickupStates(oldPickupStates);
        shipState = new ShipState(ship);
        ship.setState(initialState);
        ProjectileManager.suppressNewProjectiles(false);
        return this;
    }

    @Override
    public IGameState copy() {
        return new PickupGameState(ship, new ShipState(shipState), timestepsElapsed, pickupStates);
    }

    @Override
    public double heuristicValue() {
        return InfluenceMap.getValue(shipState.pos.x, shipState.pos.y) - (bounces * BOUNCE_PENALTY);
    }

    public ShipState getShipState() {
        return shipState;
    }

    @Override
    public double[] getFeatures() {
        // get closest living pickup
        Pickup chosen = getClosestPickup();

        // use dot product features
        Vector2d s = chosen.pos.copy().subtract(shipState.pos);
        Vector2d forward = ship.getForward();
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

    @Override
    public int getTotalTime() {
        return timestepsElapsed;
    }

    @Override
    public boolean mustBePruned() {
        return shipState.bounced;
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
